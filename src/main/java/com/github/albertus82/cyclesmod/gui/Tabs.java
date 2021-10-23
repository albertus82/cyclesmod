package com.github.albertus82.cyclesmod.gui;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.github.albertus82.cyclesmod.common.engine.CyclesModEngine;
import com.github.albertus82.cyclesmod.common.engine.NumeralSystem;
import com.github.albertus82.cyclesmod.common.engine.UnknownPropertyException;
import com.github.albertus82.cyclesmod.common.model.Gearbox;
import com.github.albertus82.cyclesmod.common.model.Power;
import com.github.albertus82.cyclesmod.common.model.Setting;
import com.github.albertus82.cyclesmod.common.model.Settings;
import com.github.albertus82.cyclesmod.common.model.Vehicle;
import com.github.albertus82.cyclesmod.common.model.VehicleType;
import com.github.albertus82.cyclesmod.common.model.VehiclesCfg;
import com.github.albertus82.cyclesmod.common.resources.Messages;
import com.github.albertus82.cyclesmod.gui.listener.LabelMouseListener;
import com.github.albertus82.cyclesmod.gui.listener.OpenPowerGraphDialogListener;
import com.github.albertus82.cyclesmod.gui.listener.PowerPropertyFocusListener;
import com.github.albertus82.cyclesmod.gui.listener.PropertyFocusListener;
import com.github.albertus82.cyclesmod.gui.listener.PropertyKeyListener;
import com.github.albertus82.cyclesmod.gui.listener.PropertyVerifyListener;
import com.github.albertus82.cyclesmod.gui.model.FormProperty;
import com.github.albertus82.cyclesmod.gui.model.GenericTextData;
import com.github.albertus82.cyclesmod.gui.model.PowerTextData;
import com.github.albertus82.cyclesmod.gui.powergraph.IPowerGraph;
import com.github.albertus82.cyclesmod.gui.powergraph.simple.PowerGraphCanvas;
import com.github.albertus82.cyclesmod.gui.resources.GuiMessages;

import it.albertus.jface.Multilanguage;
import it.albertus.jface.i18n.LocalizedWidgets;
import it.albertus.util.ISupplier;
import lombok.Getter;
import lombok.NonNull;

public class Tabs implements Multilanguage {

	private static final Messages messages = GuiMessages.INSTANCE;

	private final CyclesModGui gui;

	@Getter
	private final TextFormatter textFormatter;

	@Getter
	private final TabFolder tabFolder;

	private final Set<Label> noteLabels = new HashSet<>();

	private final Map<Mode, Map<String, FormProperty>> formProperties = new EnumMap<>(Mode.class);

	private final Map<VehicleType, PowerGraphCanvas> powerCanvases = new EnumMap<>(VehicleType.class);

	private final LocalizedWidgets localizedWidgets = new LocalizedWidgets();

	private final PropertyVerifyListener propertyVerifyListener;
	private final PropertyFocusListener propertyFocusListener;
	private final PowerPropertyFocusListener powerPropertyFocusListener;
	private final PropertyKeyListener propertyKeyListener;

	Tabs(@NonNull final CyclesModGui gui) {
		for (final Mode mode : Mode.values()) {
			formProperties.put(mode, new HashMap<>());
		}

		this.gui = gui;
		textFormatter = new TextFormatter(gui);
		propertyVerifyListener = new PropertyVerifyListener(gui);
		propertyFocusListener = new PropertyFocusListener(gui);
		powerPropertyFocusListener = new PowerPropertyFocusListener(gui);
		propertyKeyListener = new PropertyKeyListener(this);

		tabFolder = new TabFolder(gui.getShell(), SWT.NONE);
		for (final Vehicle vehicle : gui.getVehiclesInf().getVehicles().values()) {
			final TabItem tabItem = new TabItem(tabFolder, SWT.NONE);

			// This outer composite is required for GTK!
			final Composite outerComposite = new Composite(tabFolder, SWT.NONE);
			outerComposite.setLayout(new FillLayout());

			final ScrolledComposite tabScrolledComposite = new ScrolledComposite(outerComposite, SWT.V_SCROLL | SWT.H_SCROLL);
			final Composite tabComposite = new Composite(tabScrolledComposite, SWT.NONE);
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(tabComposite);

			// Settings
			final Group settingsGroup = newLocalizedGroup(tabComposite, SWT.NONE, "gui.label.settings");
			// Posizionamento dell'elemento all'interno del contenitore...
			GridDataFactory.fillDefaults().grab(false, true).applyTo(settingsGroup);
			// Definizione di come saranno disposti gli elementi contenuti...
			GridLayoutFactory.swtDefaults().numColumns(6).applyTo(settingsGroup);

			for (final Setting setting : vehicle.getSettings().getValues().keySet()) {
				final Map<Mode, String> keyMap = new EnumMap<>(Mode.class);
				for (final Mode mode : Mode.values()) {
					keyMap.put(mode, VehiclesCfg.buildPropertyKey(mode.getGame(), vehicle.getType(), Settings.PREFIX, setting.getKey()));
				}
				final Map<Mode, Integer> defaultValueMap = new EnumMap<>(Mode.class);
				for (final Mode mode : Mode.values()) {
					defaultValueMap.put(mode, gui.getDefaultProperties().get(mode).get(keyMap.get(mode)));
				}
				final Label label = newLocalizedLabel(settingsGroup, SWT.NONE, "gui.label.settings." + setting.getKey());
				GridDataFactory.swtDefaults().applyTo(label);
				label.setToolTipText(keyMap.get(gui.getMode()));
				final Text text = new Text(settingsGroup, SWT.BORDER);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(text);
				text.setData(new GenericTextData(keyMap, defaultValueMap, Settings.MAX_VALUE));
				textFormatter.setSampleNumber(text);
				text.addKeyListener(propertyKeyListener);
				text.addFocusListener(propertyFocusListener);
				text.addVerifyListener(propertyVerifyListener);
				final FormProperty formProperty = new FormProperty(label, text);
				for (final Mode game : Mode.values()) {
					formProperties.get(game).put(keyMap.get(game), formProperty);
				}
				label.addMouseListener(new LabelMouseListener(text));
			}
			final Label noteLabel = newLocalizedLabel(settingsGroup, SWT.NONE, () -> messages.get("gui.label.settings.note." + gui.getMode().getGame().toString().toLowerCase(Locale.ROOT)));
			GridDataFactory.swtDefaults().span(2, 1).align(SWT.END, SWT.CENTER).applyTo(noteLabel);
			noteLabels.add(noteLabel);

			// Power graph
			final PowerGraphCanvas canvas = new PowerGraphCanvas(tabComposite, vehicle, gui::getMode);
			canvas.addMouseListener(new OpenPowerGraphDialogListener(gui, vehicle.getType()));
			final IPowerGraph powerGraph = canvas.getPowerGraph();
			powerGraph.getXyGraph().getPlotArea().addMouseListener(new MouseListener.Stub() {
				@Override
				public void mousePressed(@NonNull final MouseEvent me) {
					if (me.button == 1) { // left button
						final FormProperty formProperty = formProperties.get(gui.getMode()).get(VehiclesCfg.buildPropertyKey(gui.getMode().getGame(), vehicle.getType(), Power.PREFIX, powerGraph.getPowerIndex(me.getLocation())));
						if (formProperty != null) {
							formProperty.getText().setFocus();
						}
					}
				}
			});
			GridDataFactory.fillDefaults().grab(true, true).span(1, 2).applyTo(canvas);
			powerCanvases.put(vehicle.getType(), canvas);

			// Gearbox
			final Group gearboxGroup = newLocalizedGroup(tabComposite, SWT.NONE, "gui.label.gearbox");
			GridDataFactory.fillDefaults().grab(false, true).applyTo(gearboxGroup);
			GridLayoutFactory.swtDefaults().numColumns(10).applyTo(gearboxGroup);

			for (int index = 0; index < vehicle.getGearbox().getRatios().length; index++) {
				final Map<Mode, String> keyMap = new EnumMap<>(Mode.class);
				for (final Mode mode : Mode.values()) {
					keyMap.put(mode, VehiclesCfg.buildPropertyKey(mode.getGame(), vehicle.getType(), Gearbox.PREFIX, index));
				}
				final Map<Mode, Integer> defaultValueMap = new EnumMap<>(Mode.class);
				for (final Mode mode : Mode.values()) {
					defaultValueMap.put(mode, gui.getDefaultProperties().get(mode).get(keyMap.get(mode)));
				}
				final Serializable gearName = index != 0 ? index : "N";
				final Label label = newLocalizedLabel(gearboxGroup, SWT.NONE, () -> messages.get("gui.label.gearbox.gear", gearName));
				GridDataFactory.swtDefaults().applyTo(label);
				label.setToolTipText(keyMap.get(gui.getMode()));
				final Text text = new Text(gearboxGroup, SWT.BORDER);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(text);
				text.setData(new GenericTextData(keyMap, defaultValueMap, Gearbox.MAX_VALUE));
				textFormatter.setSampleNumber(text);
				text.addKeyListener(propertyKeyListener);
				text.addFocusListener(propertyFocusListener);
				text.addVerifyListener(propertyVerifyListener);
				final FormProperty formProperty = new FormProperty(label, text);
				for (final Mode mode : Mode.values()) {
					formProperties.get(mode).put(keyMap.get(mode), formProperty);
				}
				label.addMouseListener(new LabelMouseListener(text));
			}

			// Power
			final Group powerGroup = newLocalizedGroup(tabComposite, SWT.NONE, "gui.label.power");
			GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(powerGroup);
			GridLayoutFactory.swtDefaults().numColumns(18).applyTo(powerGroup);

			for (int index = 0; index < vehicle.getPower().getCurve().length; index++) {
				final Map<Mode, String> keyMap = new EnumMap<>(Mode.class);
				for (final Mode mode : Mode.values()) {
					keyMap.put(mode, VehiclesCfg.buildPropertyKey(mode.getGame(), vehicle.getType(), Power.PREFIX, index));
				}
				final Map<Mode, Integer> defaultValueMap = new EnumMap<>(Mode.class);
				for (final Mode mode : Mode.values()) {
					defaultValueMap.put(mode, gui.getDefaultProperties().get(mode).get(keyMap.get(mode)));
				}
				final int rpm = Power.getRpm(index);
				final Label label = newLocalizedLabel(powerGroup, SWT.NONE, () -> messages.get("gui.label.power.rpm", rpm));
				GridDataFactory.swtDefaults().align(SWT.TRAIL, SWT.CENTER).applyTo(label);
				label.setToolTipText(keyMap.get(gui.getMode()));
				final Text text = new Text(powerGroup, SWT.BORDER);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(text);
				text.setData(new PowerTextData(keyMap, defaultValueMap, Power.MAX_VALUE, index, powerGraph));
				textFormatter.setSampleNumber(text);
				text.addKeyListener(propertyKeyListener);
				text.addFocusListener(powerPropertyFocusListener);
				text.addVerifyListener(propertyVerifyListener);
				final FormProperty formProperty = new FormProperty(label, text);
				for (final Mode mode : Mode.values()) {
					formProperties.get(mode).put(keyMap.get(mode), formProperty);
				}
				label.addMouseListener(new LabelMouseListener(text));
			}
			tabScrolledComposite.setContent(tabComposite);
			tabScrolledComposite.setExpandVertical(true);
			tabScrolledComposite.setExpandHorizontal(true);
			tabScrolledComposite.setMinSize(tabComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			tabItem.setControl(outerComposite);
		}
		updateModeSpecificWidgets();
	}

	public void updateModeSpecificWidgets() {
		for (final VehicleType vehicleType : VehicleType.values()) {
			tabFolder.getItem(vehicleType.getIndex()).setText(messages.get("gui.label.tabs." + vehicleType.getIndex() + "." + gui.getMode().getGame().toString().toLowerCase(Locale.ROOT)));
		}
		for (final Entry<String, FormProperty> entry : formProperties.get(gui.getMode()).entrySet()) {
			final String suffix = entry.getKey().substring(1 + entry.getKey().lastIndexOf('.'));
			final Setting setting = Setting.forKey(suffix);
			if (setting != null) {
				entry.getValue().getLabel().setEnabled(setting.getGames().contains(gui.getMode().getGame()));
			}
			if (entry.getKey().toUpperCase(Locale.ROOT).contains(Gearbox.PREFIX.toUpperCase(Locale.ROOT)) && CyclesModEngine.isNumeric(suffix, NumeralSystem.DECIMAL.getRadix())) {
				entry.getValue().getLabel().setEnabled(Gearbox.getValidGears().get(gui.getMode().getGame()).contains(Byte.valueOf(suffix)));
			}
		}
		for (final Label noteLabel : noteLabels) {
			noteLabel.setEnabled(Mode.GPC.equals(gui.getMode()));
			noteLabel.setText(messages.get("gui.label.settings.note." + gui.getMode().getGame().toString().toLowerCase(Locale.ROOT)));
			noteLabel.requestLayout();
		}
		for (final PowerGraphCanvas canvas : powerCanvases.values()) {
			canvas.updateModeSpecificWidgets();
		}
	}

	@Override
	public void updateLanguage() {
		localizedWidgets.resetAllTexts();
		for (final Multilanguage canvas : powerCanvases.values()) {
			canvas.updateLanguage();
		}

		// Update form fields...
		disableTextListeners();
		for (final FormProperty formProperty : formProperties.get(gui.getMode()).values()) {
			formProperty.backup();
			final Text text = formProperty.getText();
			text.setVisible(false);
			textFormatter.setSampleNumber(text);
		}
		gui.getShell().layout(true, true);
		for (final FormProperty formProperty : formProperties.get(gui.getMode()).values()) {
			formProperty.restore();
			final Text text = formProperty.getText();
			final String toolTipText = messages.get("gui.message.tooltip.default", Integer.toString(((GenericTextData) text.getData()).getDefaultValueMap().get(gui.getMode()), gui.getNumeralSystem().getRadix()).toUpperCase(Locale.ROOT));
			if (text.getToolTipText() == null || !text.getToolTipText().equals(toolTipText)) {
				text.setToolTipText(toolTipText);
			}
		}
		enableTextListeners();
	}

	public void updateFormValues() {
		final Map<String, Integer> properties = new VehiclesCfg(gui.getMode().getGame(), gui.getVehiclesInf()).getMap();

		// Consistency check...
		if (properties.size() != formProperties.get(gui.getMode()).size()) {
			throw new IllegalStateException(messages.get("gui.error.properties.number"));
		}

		// Update screen values...
		disableTextListeners();
		updateFields(properties);
		enableTextListeners();

		// Update power graphs...
		for (final Vehicle vehicle : gui.getVehiclesInf().getVehicles().values()) {
			final IPowerGraph powerGraph = powerCanvases.get(vehicle.getType()).getPowerGraph();
			for (short i = 0; i < vehicle.getPower().getCurve().length; i++) {
				powerGraph.setPowerValue(i, vehicle.getPower().getCurve()[i]);
			}
			powerGraph.refresh();
		}

		propertyFocusListener.reset();
	}

	private void updateFields(final Map<String, Integer> properties) {
		for (final Entry<String, FormProperty> entry : formProperties.get(gui.getMode()).entrySet()) {
			if (!properties.containsKey(entry.getKey())) {
				throw new IllegalStateException(messages.get("gui.error.property.missing", entry.getKey()));
			}
			final Label label = entry.getValue().getLabel();
			final Text text = entry.getValue().getText();

			// Update field max length...
			final int textLimit;
			if (CyclesModEngine.isSettingsProperty(entry.getKey())) {
				textLimit = Integer.toString(Settings.MAX_VALUE, gui.getNumeralSystem().getRadix()).length();
			}
			else if (CyclesModEngine.isGearboxProperty(entry.getKey())) {
				textLimit = Integer.toString(Gearbox.MAX_VALUE, gui.getNumeralSystem().getRadix()).length();
			}
			else if (CyclesModEngine.isPowerProperty(entry.getKey())) {
				textLimit = Integer.toString(Power.MAX_VALUE, gui.getNumeralSystem().getRadix()).length();
			}
			else {
				throw new IllegalArgumentException(entry.getKey(), new UnknownPropertyException(entry.getKey()));
			}
			if (text.getTextLimit() != textLimit) {
				text.setTextLimit(textLimit);
			}

			// Update label tooltip...
			final String labelToolTip = entry.getKey();
			if (label.getToolTipText() == null || !label.getToolTipText().equals(labelToolTip)) {
				label.setToolTipText(labelToolTip);
			}

			// Update text value...
			final String textValue = Integer.toString(properties.get(entry.getKey()), gui.getNumeralSystem().getRadix()).toUpperCase(Locale.ROOT);
			if (!text.getText().equals(textValue)) {
				text.setText(textValue);
			}

			// Update text tooltip...
			final String textToolTip = messages.get("gui.message.tooltip.default", Integer.toString(((GenericTextData) text.getData()).getDefaultValueMap().get(gui.getMode()), gui.getNumeralSystem().getRadix()).toUpperCase(Locale.ROOT));
			if (text.getToolTipText() == null || !text.getToolTipText().equals(textToolTip)) {
				text.setToolTipText(textToolTip);
			}

			// Update font style...
			textFormatter.updateFontStyle(text);
		}
	}

	private void enableTextListeners() {
		propertyKeyListener.setEnabled(true);
		propertyVerifyListener.setEnabled(true);
		propertyFocusListener.setEnabled(true);
		powerPropertyFocusListener.setEnabled(true);
	}

	private void disableTextListeners() {
		propertyKeyListener.setEnabled(false);
		propertyVerifyListener.setEnabled(false);
		propertyFocusListener.setEnabled(false);
		powerPropertyFocusListener.setEnabled(false);
	}

	public Map<Mode, Map<String, FormProperty>> getFormProperties() {
		return Collections.unmodifiableMap(formProperties);
	}

	private Group newLocalizedGroup(@NonNull final Composite parent, final int style, @NonNull final String messageKey) {
		return newLocalizedGroup(parent, style, () -> messages.get(messageKey));
	}

	private Group newLocalizedGroup(@NonNull final Composite parent, final int style, @NonNull final ISupplier<String> textSupplier) {
		return localizedWidgets.putAndReturn(new Group(parent, style), textSupplier).getKey();
	}

	private Label newLocalizedLabel(@NonNull final Composite parent, final int style, @NonNull final String messageKey) {
		return newLocalizedLabel(parent, style, () -> messages.get(messageKey));
	}

	private Label newLocalizedLabel(@NonNull final Composite parent, final int style, @NonNull final ISupplier<String> textSupplier) {
		return localizedWidgets.putAndReturn(new Label(parent, style), textSupplier).getKey();
	}

}

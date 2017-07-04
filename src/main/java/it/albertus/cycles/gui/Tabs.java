package it.albertus.cycles.gui;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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

import it.albertus.cycles.gui.FormProperty.LabelDataKey;
import it.albertus.cycles.gui.FormProperty.TextDataKey;
import it.albertus.cycles.gui.listener.OpenTorqueGraphDialogListener;
import it.albertus.cycles.gui.listener.PropertyFocusListener;
import it.albertus.cycles.gui.listener.PropertyKeyListener;
import it.albertus.cycles.gui.listener.PropertyVerifyListener;
import it.albertus.cycles.gui.listener.TorquePropertyFocusListener;
import it.albertus.cycles.gui.torquegraph.ITorqueGraph;
import it.albertus.cycles.gui.torquegraph.simple.TorqueGraphCanvas;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.Bike.BikeType;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Setting;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;

public class Tabs {

	private final CyclesModGui gui;

	private final TextFormatter textFormatter;

	private final TabFolder tabFolder;

	private final Map<String, FormProperty> formProperties = new HashMap<String, FormProperty>();

	private final Map<BikeType, Group> settingsGroups = new EnumMap<BikeType, Group>(BikeType.class);
	private final Map<BikeType, Group> gearboxGroups = new EnumMap<BikeType, Group>(BikeType.class);
	private final Map<BikeType, Group> torqueGroups = new EnumMap<BikeType, Group>(BikeType.class);
	private final Map<BikeType, TorqueGraphCanvas> torqueCanvases = new EnumMap<BikeType, TorqueGraphCanvas>(BikeType.class);

	private final PropertyVerifyListener propertyVerifyListener;
	private final PropertyFocusListener propertyFocusListener;
	private final TorquePropertyFocusListener torquePropertyFocusListener;
	private final PropertyKeyListener propertyKeyListener;

	Tabs(final CyclesModGui gui) {
		this.gui = gui;
		textFormatter = new TextFormatter(gui);
		propertyVerifyListener = new PropertyVerifyListener(gui);
		propertyFocusListener = new PropertyFocusListener(gui);
		torquePropertyFocusListener = new TorquePropertyFocusListener(gui);
		propertyKeyListener = new PropertyKeyListener(gui);

		tabFolder = new TabFolder(gui.getShell(), SWT.NULL);
		for (final Bike bike : gui.getBikesInf().getBikes()) {
			final TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
			tabItem.setText(bike.getType().getDisplacement() + " cc");

			// This outer composite is required for GTK!
			final Composite outerComposite = new Composite(tabFolder, SWT.NONE);
			outerComposite.setLayout(new FillLayout());

			final ScrolledComposite tabScrolledComposite = new ScrolledComposite(outerComposite, SWT.V_SCROLL | SWT.H_SCROLL);
			final Composite tabComposite = new Composite(tabScrolledComposite, SWT.NONE);
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(tabComposite);

			// Settings
			final Group settingsGroup = new Group(tabComposite, SWT.NULL);
			settingsGroup.setText(Messages.get("lbl.settings"));
			// Posizionamento dell'elemento all'interno del contenitore...
			GridDataFactory.fillDefaults().grab(false, true).applyTo(settingsGroup);
			// Definizione di come saranno disposti gli elementi contenuti...
			GridLayoutFactory.swtDefaults().numColumns(6).applyTo(settingsGroup);
			settingsGroups.put(bike.getType(), settingsGroup);

			for (final Setting setting : bike.getSettings().getValues().keySet()) {
				final String key = BikesCfg.buildPropertyKey(bike.getType(), Settings.class, setting.toString());
				final Integer defaultValue = gui.getDefaultProperties().get(key);
				final Label label = new Label(settingsGroup, SWT.NULL);
				GridDataFactory.swtDefaults().applyTo(label);
				final String labelTextKey = "lbl." + setting.toString();
				label.setText(Messages.get(labelTextKey));
				label.setData(LabelDataKey.KEY.toString(), labelTextKey);
				label.setToolTipText(key);
				final Text text = new Text(settingsGroup, SWT.BORDER);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(text);
				final int textSize = Integer.toString(Settings.MAX_VALUE).length();
				text.setData(TextDataKey.DEFAULT.toString(), defaultValue);
				text.setData(TextDataKey.KEY.toString(), key);
				text.setData(TextDataKey.SIZE.toString(), textSize);
				text.setData(TextDataKey.MAX.toString(), Settings.MAX_VALUE);
				textFormatter.setSampleNumber(text);
				text.addKeyListener(propertyKeyListener);
				text.addFocusListener(propertyFocusListener);
				text.addVerifyListener(propertyVerifyListener);
				formProperties.put(key, new FormProperty(label, text));
			}

			// Torque graph
			final TorqueGraphCanvas canvas = new TorqueGraphCanvas(tabComposite, bike);
			canvas.addMouseListener(new OpenTorqueGraphDialogListener(gui, bike.getType()));
			final ITorqueGraph torqueGraph = canvas.getTorqueGraph();
			torqueGraph.getXyGraph().getPlotArea().addMouseListener(new MouseListener.Stub() {
				@Override
				public void mousePressed(final MouseEvent me) {
					if (me.button == 1) { // left button
						final FormProperty formProperty = formProperties.get(BikesCfg.buildPropertyKey(bike.getType(), Torque.class, torqueGraph.getTorqueIndex(me.getLocation())));
						if (formProperty != null) {
							formProperty.getText().setFocus();
						}
					}
				}
			});
			GridDataFactory.fillDefaults().grab(true, true).span(1, 2).applyTo(canvas);
			torqueCanvases.put(bike.getType(), canvas);

			// Gearbox
			final Group gearboxGroup = new Group(tabComposite, SWT.NULL);
			gearboxGroup.setText(Messages.get("lbl.gearbox"));
			GridDataFactory.fillDefaults().grab(false, true).applyTo(gearboxGroup);
			GridLayoutFactory.swtDefaults().numColumns(10).applyTo(gearboxGroup);
			gearboxGroups.put(bike.getType(), gearboxGroup);

			for (int index = 0; index < bike.getGearbox().getRatios().length; index++) {
				final String key = BikesCfg.buildPropertyKey(bike.getType(), Gearbox.class, index);
				final Integer defaultValue = gui.getDefaultProperties().get(key);
				final Label label = new Label(gearboxGroup, SWT.NULL);
				GridDataFactory.swtDefaults().applyTo(label);
				final String labelTextKey = "lbl.gear";
				final String labelTextArgument = index != 0 ? String.valueOf(index) : "N";
				label.setText(Messages.get(labelTextKey, labelTextArgument));
				label.setData(LabelDataKey.KEY.toString(), labelTextKey);
				label.setData(LabelDataKey.ARGUMENT.toString(), labelTextArgument);
				label.setToolTipText(key);
				final Text text = new Text(gearboxGroup, SWT.BORDER);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(text);
				final int textSize = Integer.toString(Gearbox.MAX_VALUE).length();
				text.setData(TextDataKey.DEFAULT.toString(), defaultValue);
				text.setData(TextDataKey.KEY.toString(), key);
				text.setData(TextDataKey.SIZE.toString(), textSize);
				text.setData(TextDataKey.MAX.toString(), Gearbox.MAX_VALUE);
				textFormatter.setSampleNumber(text);
				text.addKeyListener(propertyKeyListener);
				text.addFocusListener(propertyFocusListener);
				text.addVerifyListener(propertyVerifyListener);
				formProperties.put(key, new FormProperty(label, text));
			}

			// Torque
			final Group torqueGroup = new Group(tabComposite, SWT.NULL);
			torqueGroup.setText(Messages.get("lbl.torque"));
			GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(torqueGroup);
			GridLayoutFactory.swtDefaults().numColumns(18).applyTo(torqueGroup);
			torqueGroups.put(bike.getType(), torqueGroup);

			for (int index = 0; index < bike.getTorque().getCurve().length; index++) {
				final String key = BikesCfg.buildPropertyKey(bike.getType(), Torque.class, index);
				final Integer defaultValue = gui.getDefaultProperties().get(key);
				final Label label = new Label(torqueGroup, SWT.NULL);
				GridDataFactory.swtDefaults().align(SWT.TRAIL, SWT.CENTER).applyTo(label);
				final String labelTextKey = "lbl.rpm";
				final String labelTextArgument = String.valueOf(Torque.getRpm(index));
				label.setText(Messages.get(labelTextKey, labelTextArgument));
				label.setData(LabelDataKey.KEY.toString(), labelTextKey);
				label.setData(LabelDataKey.ARGUMENT.toString(), labelTextArgument);
				label.setToolTipText(key);
				final Text text = new Text(torqueGroup, SWT.BORDER);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(text);
				final int textSize = Integer.toString(Torque.MAX_VALUE).length();
				text.setData(TextDataKey.DEFAULT.toString(), defaultValue);
				text.setData(TextDataKey.KEY.toString(), key);
				text.setData(TextDataKey.GRAPH.toString(), torqueGraph);
				text.setData(TextDataKey.INDEX.toString(), index);
				text.setData(TextDataKey.SIZE.toString(), textSize);
				text.setData(TextDataKey.MAX.toString(), Integer.valueOf(Torque.MAX_VALUE));
				textFormatter.setSampleNumber(text);
				text.addKeyListener(propertyKeyListener);
				text.addFocusListener(torquePropertyFocusListener);
				text.addVerifyListener(propertyVerifyListener);
				formProperties.put(key, new FormProperty(label, text));
			}
			tabScrolledComposite.setContent(tabComposite);
			tabScrolledComposite.setExpandVertical(true);
			tabScrolledComposite.setExpandHorizontal(true);
			tabScrolledComposite.setMinSize(tabComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			tabItem.setControl(outerComposite);
		}
	}

	public void updateTexts() {
		for (final Group settingsGroup : settingsGroups.values()) {
			settingsGroup.setText(Messages.get("lbl.settings"));
		}
		for (final Group gearboxGroup : gearboxGroups.values()) {
			gearboxGroup.setText(Messages.get("lbl.gearbox"));
		}
		for (final Group torqueGroup : torqueGroups.values()) {
			torqueGroup.setText(Messages.get("lbl.torque"));
		}
		for (final TorqueGraphCanvas canvas : torqueCanvases.values()) {
			canvas.updateTexts();
		}

		// Update form fields...
		disableTextListeners();
		for (final FormProperty formProperty : formProperties.values()) {
			final Label label = formProperty.getLabel();
			final String updatedLabelText = Messages.get((String) label.getData(LabelDataKey.KEY.toString()), label.getData(LabelDataKey.ARGUMENT.toString()));
			if (!label.getText().equals(updatedLabelText)) {
				label.setText(updatedLabelText);
			}
			formProperty.backup();
			final Text text = formProperty.getText();
			text.setVisible(false);
			textFormatter.setSampleNumber(text);
		}
		gui.getShell().layout(true, true);
		for (final FormProperty formProperty : formProperties.values()) {
			formProperty.restore();
		}
		enableTextListeners();
	}

	public void updateFormValues() {
		final Map<String, Integer> properties = new BikesCfg(gui.getBikesInf()).getMap();

		// Consistency check...
		if (properties.size() != formProperties.size()) {
			throw new IllegalStateException(Messages.get("err.properties.number"));
		}

		// Update screen values...
		disableTextListeners();
		updateFields(properties);
		enableTextListeners();

		// Update torque graphs...
		for (final Bike bike : gui.getBikesInf().getBikes()) {
			final ITorqueGraph torqueGraph = torqueCanvases.get(bike.getType()).getTorqueGraph();
			for (short i = 0; i < bike.getTorque().getCurve().length; i++) {
				torqueGraph.getValues()[i] = bike.getTorque().getCurve()[i];
			}
			torqueGraph.refresh();
		}
	}

	private void updateFields(final Map<String, Integer> properties) {
		for (final Entry<String, FormProperty> entry : formProperties.entrySet()) {
			if (!properties.containsKey(entry.getKey())) {
				throw new IllegalStateException(Messages.get("err.property.missing", entry.getKey()));
			}
			final Text field = entry.getValue().getText();

			// Update field max length...
			final int textLimit;
			if (gui.isSettingsProperty(entry.getKey())) {
				textLimit = Integer.toString(Settings.MAX_VALUE, gui.getNumeralSystem().getRadix()).length();
			}
			else if (gui.isGearboxProperty(entry.getKey())) {
				textLimit = Integer.toString(Gearbox.MAX_VALUE, gui.getNumeralSystem().getRadix()).length();
			}
			else if (gui.isTorqueProperty(entry.getKey())) {
				textLimit = Integer.toString(Torque.MAX_VALUE, gui.getNumeralSystem().getRadix()).length();
			}
			else {
				throw new IllegalStateException(Messages.get("err.unsupported.property", entry.getKey(), entry.getValue().getValue()));
			}
			if (field.getTextLimit() != textLimit) {
				field.setTextLimit(textLimit);
			}

			// Update field value...
			final String text = Integer.toString(properties.get(entry.getKey()), gui.getNumeralSystem().getRadix()).toUpperCase();
			if (!field.getText().equals(text)) {
				field.setText(text);
			}

			// Update tooltip text...
			final String toolTipText = Messages.get("msg.tooltip.default", Integer.toString((Integer) field.getData(FormProperty.TextDataKey.DEFAULT.toString()), gui.getNumeralSystem().getRadix()).toUpperCase());
			if (field.getToolTipText() == null || !field.getToolTipText().equals(toolTipText)) {
				field.setToolTipText(toolTipText);
			}

			// Update font style...
			textFormatter.updateFontStyle(field);
		}
	}

	private void enableTextListeners() {
		propertyKeyListener.setEnabled(true);
		propertyVerifyListener.setEnabled(true);
		propertyFocusListener.setEnabled(true);
		torquePropertyFocusListener.setEnabled(true);
	}

	private void disableTextListeners() {
		propertyKeyListener.setEnabled(false);
		propertyVerifyListener.setEnabled(false);
		propertyFocusListener.setEnabled(false);
		torquePropertyFocusListener.setEnabled(false);
	}

	public TabFolder getTabFolder() {
		return tabFolder;
	}

	public TextFormatter getTextFormatter() {
		return textFormatter;
	}

	public Map<String, FormProperty> getFormProperties() {
		return Collections.unmodifiableMap(formProperties);
	}

	public Map<BikeType, TorqueGraphCanvas> getTorqueCanvases() {
		return Collections.unmodifiableMap(torqueCanvases);
	}

	public Map<BikeType, Group> getSettingsGroups() {
		return Collections.unmodifiableMap(settingsGroups);
	}

	public Map<BikeType, Group> getGearboxGroups() {
		return Collections.unmodifiableMap(gearboxGroups);
	}

	public Map<BikeType, Group> getTorqueGroups() {
		return Collections.unmodifiableMap(torqueGroups);
	}

}

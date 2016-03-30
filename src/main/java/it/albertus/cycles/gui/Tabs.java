package it.albertus.cycles.gui;

import it.albertus.cycles.gui.FormProperty.LabelDataKey;
import it.albertus.cycles.gui.FormProperty.TextDataKey;
import it.albertus.cycles.gui.listener.PropertyFocusListener;
import it.albertus.cycles.gui.listener.PropertyVerifyListener;
import it.albertus.cycles.gui.listener.TorquePropertyFocusListener;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.Bike.BikeType;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Setting;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Resources;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

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

public class Tabs {

	private final CyclesModGui gui;

	private final TextFormatter textFormatter; 

	private final TabFolder tabFolder;

	private final Map<String, FormProperty> formProperties = new HashMap<String, FormProperty>();

	private final Map<BikeType, Group> settingsGroups = new EnumMap<BikeType, Group>(BikeType.class);
	private final Map<BikeType, Group> gearboxGroups = new EnumMap<BikeType, Group>(BikeType.class);
	private final Map<BikeType, Group> torqueGroups = new EnumMap<BikeType, Group>(BikeType.class);
	private final Map<BikeType, TorqueGraph> torqueGraphs = new EnumMap<BikeType, TorqueGraph>(BikeType.class);

	private final PropertyVerifyListener propertyVerifyListener;
	private final PropertyFocusListener propertyFocusListener;
	private final TorquePropertyFocusListener torquePropertyFocusListener;

	public Tabs(final CyclesModGui gui) {
		if (this.tabFolder != null) {
			throw new IllegalStateException("Only one instance is allowed");
		}

		this.gui = gui;
		textFormatter = new TextFormatter(gui);
		propertyVerifyListener = new PropertyVerifyListener(gui);
		propertyFocusListener = new PropertyFocusListener(gui);
		torquePropertyFocusListener = new TorquePropertyFocusListener(gui);

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
			settingsGroup.setText(Resources.get("lbl.settings"));
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
				label.setText(Resources.get(labelTextKey));
				label.setData(LabelDataKey.KEY.toString(), labelTextKey);
				label.setToolTipText(key);
				final Text text = new Text(settingsGroup, SWT.BORDER);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(text);
				final int textSize = Integer.toString(Settings.MAX_VALUE).length();
				text.setData(TextDataKey.DEFAULT.toString(), defaultValue);
				text.setData(TextDataKey.KEY.toString(), key);
				text.setData(TextDataKey.SIZE.toString(), textSize);
				textFormatter.setSampleNumber(text);
				text.addFocusListener(propertyFocusListener);
				text.addVerifyListener(propertyVerifyListener);
				formProperties.put(key, new FormProperty(label, text));
			}

			// Torque graph
			final TorqueGraph graph = new TorqueGraph(tabComposite, bike);
			GridDataFactory.fillDefaults().grab(true, true).span(1, 2).applyTo(graph);
			torqueGraphs.put(bike.getType(), graph);

			// Gearbox
			final Group gearboxGroup = new Group(tabComposite, SWT.NULL);
			gearboxGroup.setText(Resources.get("lbl.gearbox"));
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
				label.setText(Resources.get(labelTextKey, labelTextArgument));
				label.setData(LabelDataKey.KEY.toString(), labelTextKey);
				label.setData(LabelDataKey.ARGUMENT.toString(), labelTextArgument);
				label.setToolTipText(key);
				final Text text = new Text(gearboxGroup, SWT.BORDER);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(text);
				final int textSize = Integer.toString(Gearbox.MAX_VALUE).length();
				text.setData(TextDataKey.DEFAULT.toString(), defaultValue);
				text.setData(TextDataKey.KEY.toString(), key);
				text.setData(TextDataKey.SIZE.toString(), textSize);
				textFormatter.setSampleNumber(text);
				text.addFocusListener(propertyFocusListener);
				text.addVerifyListener(propertyVerifyListener);
				formProperties.put(key, new FormProperty(label, text));
			}

			// Torque
			final Group torqueGroup = new Group(tabComposite, SWT.NULL);
			torqueGroup.setText(Resources.get("lbl.torque"));
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
				label.setText(Resources.get(labelTextKey, labelTextArgument));
				label.setData(LabelDataKey.KEY.toString(), labelTextKey);
				label.setData(LabelDataKey.ARGUMENT.toString(), labelTextArgument);
				label.setToolTipText(key);
				final Text text = new Text(torqueGroup, SWT.BORDER);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(text);
				final int textSize = Integer.toString(Torque.MAX_VALUE).length();
				text.setData(TextDataKey.DEFAULT.toString(), defaultValue);
				text.setData(TextDataKey.KEY.toString(), key);
				text.setData(TextDataKey.GRAPH.toString(), graph);
				text.setData(TextDataKey.INDEX.toString(), index);
				text.setData(TextDataKey.SIZE.toString(), textSize);
				textFormatter.setSampleNumber(text);
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
			settingsGroup.setText(Resources.get("lbl.settings"));
		}
		for (final Group gearboxGroup : gearboxGroups.values()) {
			gearboxGroup.setText(Resources.get("lbl.gearbox"));
		}
		for (final Group torqueGroup : torqueGroups.values()) {
			torqueGroup.setText(Resources.get("lbl.torque"));
		}
		for (final TorqueGraph torqueGraph : torqueGraphs.values()) {
			torqueGraph.updateTexts();
		}

		// Update form fields...
		disableTextListeners();
		for (final FormProperty formProperty : formProperties.values()) {
			final Label label = formProperty.getLabel();
			final String updatedLabelText = Resources.get((String) label.getData(LabelDataKey.KEY.toString()), label.getData(LabelDataKey.ARGUMENT.toString()));
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
			throw new IllegalStateException(Resources.get("err.properties.number"));
		}

		// Update screen values...
		disableTextListeners();
		for (final String key : formProperties.keySet()) {
			if (!properties.containsKey(key)) {
				throw new RuntimeException(Resources.get("err.property.missing", key));
			}
			final Text field = formProperties.get(key).getText();

			// Update field max length...
			final int textLimit;
			if (gui.isSettingsProperty(key)) {
				textLimit = Integer.toString(Settings.MAX_VALUE, gui.getNumeralSystem().getRadix()).length();
			}
			else if (gui.isGearboxProperty(key)) {
				textLimit = Integer.toString(Gearbox.MAX_VALUE, gui.getNumeralSystem().getRadix()).length();
			}
			else if (gui.isTorqueProperty(key)) {
				textLimit = Integer.toString(Torque.MAX_VALUE, gui.getNumeralSystem().getRadix()).length();
			}
			else {
				throw new IllegalStateException(Resources.get("err.unsupported.property", key, formProperties.get(key).getValue()));
			}
			if (field.getTextLimit() != textLimit) {
				field.setTextLimit(textLimit);
			}

			// Update field value...
			final String text = Integer.toString(properties.get(key), gui.getNumeralSystem().getRadix()).toUpperCase();
			if (!field.getText().equals(text)) {
				field.setText(text);
			}

			// Update tooltip text...
			final String toolTipText = Resources.get("msg.tooltip.default", Integer.toString(((Integer) field.getData(FormProperty.TextDataKey.DEFAULT.toString())), gui.getNumeralSystem().getRadix()).toUpperCase());
			if (field.getToolTipText() == null || !field.getToolTipText().equals(toolTipText)) {
				field.setToolTipText(toolTipText);
			}

			// Update font style...
			textFormatter.updateFontStyle(field);
		}
		enableTextListeners();

		// Update torque graphs...
		for (final Bike bike : gui.getBikesInf().getBikes()) {
			final TorqueGraph graph = torqueGraphs.get(bike.getType());
			for (short i = 0; i < bike.getTorque().getCurve().length; i++) {
				graph.getValues()[i] = bike.getTorque().getCurve()[i];
			}
			graph.refresh();
		}
	}

	private void enableTextListeners() {
		propertyVerifyListener.setEnabled(true);
		propertyFocusListener.setEnabled(true);
		torquePropertyFocusListener.setEnabled(true);
	}

	private void disableTextListeners() {
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

	public Map<BikeType, TorqueGraph> getTorqueGraphs() {
		return Collections.unmodifiableMap(torqueGraphs);
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

package it.albertus.cycles.gui;

import it.albertus.cycles.gui.listener.PropertyFocusListener;
import it.albertus.cycles.gui.listener.PropertyVerifyListener;
import it.albertus.cycles.gui.listener.TorquePropertyFocusListener;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Setting;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Resources;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class Tabs {

	private static final char SAMPLE_CHAR = '9';

	private final TabFolder tabFolder;

	public Tabs(final CyclesModGui gui) {
		tabFolder = new TabFolder(gui.getShell(), SWT.NULL);
		for (final Bike bike : gui.getBikesInf().getBikes()) {
			final TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
			tabItem.setText(bike.getType().getDisplacement() + " cc");

			final Composite tabComposite = new Composite(tabFolder, SWT.NULL);
			tabItem.setControl(tabComposite);
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(tabComposite);

			// Settings
			final Group settingsGroup = new Group(tabComposite, SWT.NULL);
			settingsGroup.setText(Resources.get("lbl.settings"));
			// Posizionamento dell'elemento all'interno del contenitore...
			GridDataFactory.fillDefaults().grab(false, true).applyTo(settingsGroup);
			// Definizione di come saranno disposti gli elementi contenuti...
			GridLayoutFactory.swtDefaults().numColumns(6).applyTo(settingsGroup);

			for (final Setting setting : bike.getSettings().getValues().keySet()) {
				final String key = BikesCfg.buildPropertyKey(bike.getType(), Settings.class, setting.toString());
				final String defaultValue = gui.getDefaultProperties().getProperty(key);
				final Label label = new Label(settingsGroup, SWT.NULL);
				label.setText(Resources.get("lbl." + setting.toString()));
				label.setToolTipText(key);
				final Text text = new Text(settingsGroup, SWT.BORDER);
				final int maxFieldSize = Integer.toString(Settings.MAX_VALUE).length();
				setSampleNumber(text, maxFieldSize);
				text.setTextLimit(maxFieldSize);
				text.setToolTipText(Resources.get("msg.tooltip.default", defaultValue));
				GridDataFactory.swtDefaults().grab(true, false).applyTo(text);
				text.addFocusListener(new PropertyFocusListener(defaultValue));
				text.addListener(SWT.Verify, new PropertyVerifyListener());
				gui.getFormProperties().put(key, new FormProperty(label, text));
			}

			// Torque graph
			final TorqueGraph graph = new TorqueGraph(tabComposite, bike);
			gui.getTorqueGraphs().put(bike.getType(), graph);

			// Gearbox
			final Group gearboxGroup = new Group(tabComposite, SWT.NULL);
			gearboxGroup.setText(Resources.get("lbl.gearbox"));
			GridDataFactory.fillDefaults().grab(false, true).applyTo(gearboxGroup);
			GridLayoutFactory.swtDefaults().numColumns(10).applyTo(gearboxGroup);

			for (int index = 0; index < bike.getGearbox().getRatios().length; index++) {
				final String key = BikesCfg.buildPropertyKey(bike.getType(), Gearbox.class, index);
				final String defaultValue = gui.getDefaultProperties().getProperty(key);
				final Label label = new Label(gearboxGroup, SWT.NULL);
				label.setText(Resources.get("lbl.gear", index != 0 ? index : "N"));
				label.setToolTipText(key);
				final Text text = new Text(gearboxGroup, SWT.BORDER);
				final int maxFieldSize = Integer.toString(Gearbox.MAX_VALUE).length();
				setSampleNumber(text, maxFieldSize);
				text.setTextLimit(maxFieldSize);
				text.setToolTipText(Resources.get("msg.tooltip.default", defaultValue));
				GridDataFactory.swtDefaults().grab(true, false).applyTo(text);
				text.addFocusListener(new PropertyFocusListener(defaultValue));
				text.addListener(SWT.Verify, new PropertyVerifyListener());
				gui.getFormProperties().put(key, new FormProperty(label, text));
			}

			// Torque
			final Group torqueGroup = new Group(tabComposite, SWT.NULL);
			torqueGroup.setText(Resources.get("lbl.torque"));
			GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(torqueGroup);
			GridLayoutFactory.swtDefaults().numColumns(18).applyTo(torqueGroup);

			for (int index = 0; index < bike.getTorque().getCurve().length; index++) {
				final String key = BikesCfg.buildPropertyKey(bike.getType(), Torque.class, index);
				final String defaultValue = gui.getDefaultProperties().getProperty(key);
				final Label label = new Label(torqueGroup, SWT.NULL);
				label.setText(Resources.get("lbl.rpm", Torque.getRpm(index)));
				label.setToolTipText(key);
				final Text text = new Text(torqueGroup, SWT.BORDER);
				final int maxFieldSize = Short.toString(Torque.MAX_VALUE).length();
				setSampleNumber(text, maxFieldSize);
				text.setTextLimit(maxFieldSize);
				text.setToolTipText(Resources.get("msg.tooltip.default", defaultValue));
				GridDataFactory.swtDefaults().grab(true, false).applyTo(text);
				text.addFocusListener(new TorquePropertyFocusListener(defaultValue, key, graph));
				text.addListener(SWT.Verify, new PropertyVerifyListener());
				gui.getFormProperties().put(key, new FormProperty(label, text));
			}
		}
	}

	/** Consente la determinazione automatica della larghezza del campo. */
	private void setSampleNumber(final Text text, final int size) {
		final char[] sample = new char[size];
		for (int i = 0; i < size; i++) {
			sample[i] = SAMPLE_CHAR;
		}
		text.setText(String.valueOf(sample));
		PropertyFormatter.getInstance().setBoldFontStyle(text);
	}

	public TabFolder getTabFolder() {
		return tabFolder;
	}

}

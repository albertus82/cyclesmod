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
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
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

			for (final Setting setting : bike.getSettings().getValues().keySet()) {
				final String key = BikesCfg.buildPropertyKey(bike.getType(), Settings.class, setting.toString());
				final Integer defaultValue = gui.getDefaultProperties().get(key);
				final Label label = new Label(settingsGroup, SWT.NULL);
				GridDataFactory.swtDefaults().applyTo(label);
				label.setText(Resources.get("lbl." + setting.toString()));
				label.setToolTipText(key);
				final Text text = new Text(settingsGroup, SWT.BORDER);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(text);
				final int maxFieldSize = Integer.toString(Settings.MAX_VALUE).length();
				setSampleNumber(text, maxFieldSize);
				text.setTextLimit(maxFieldSize);
				text.setToolTipText(Resources.get("msg.tooltip.default", defaultValue.toString()));
				text.setData(FormProperty.KEY_DEFAULT_VALUE, defaultValue);
				text.addFocusListener(new PropertyFocusListener());
				text.addListener(SWT.Verify, new PropertyVerifyListener());
				gui.getFormProperties().put(key, new FormProperty(label, text));
			}

			// Torque graph
			final TorqueGraph graph = new TorqueGraph(tabComposite, bike);
			GridDataFactory.fillDefaults().grab(true, true).span(1, 2).applyTo(graph);
			gui.getTorqueGraphs().put(bike.getType(), graph);

			// Gearbox
			final Group gearboxGroup = new Group(tabComposite, SWT.NULL);
			gearboxGroup.setText(Resources.get("lbl.gearbox"));
			GridDataFactory.fillDefaults().grab(false, true).applyTo(gearboxGroup);
			GridLayoutFactory.swtDefaults().numColumns(10).applyTo(gearboxGroup);

			for (int index = 0; index < bike.getGearbox().getRatios().length; index++) {
				final String key = BikesCfg.buildPropertyKey(bike.getType(), Gearbox.class, index);
				final Integer defaultValue = gui.getDefaultProperties().get(key);
				final Label label = new Label(gearboxGroup, SWT.NULL);
				GridDataFactory.swtDefaults().applyTo(label);
				label.setText(Resources.get("lbl.gear", index != 0 ? index : "N"));
				label.setToolTipText(key);
				final Text text = new Text(gearboxGroup, SWT.BORDER);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(text);
				final int maxFieldSize = Integer.toString(Gearbox.MAX_VALUE).length();
				setSampleNumber(text, maxFieldSize);
				text.setTextLimit(maxFieldSize);
				text.setToolTipText(Resources.get("msg.tooltip.default", defaultValue));
				text.setData(FormProperty.KEY_DEFAULT_VALUE, defaultValue);
				text.addFocusListener(new PropertyFocusListener());
				text.addListener(SWT.Verify, new PropertyVerifyListener());
				gui.getFormProperties().put(key, new FormProperty(label, text));
			}

			// Torque
			final Group torqueGroup = new Group(tabComposite, SWT.NULL);
			torqueGroup.setText(Resources.get("lbl.torque"));
			GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(torqueGroup);
			GridLayoutFactory.swtDefaults().numColumns(18).applyTo(torqueGroup);

			for (int index = 0; index < bike.getTorque().getCurve().length; index++) {
				final String key = BikesCfg.buildPropertyKey(bike.getType(), Torque.class, index);
				final Integer defaultValue = gui.getDefaultProperties().get(key);
				final Label label = new Label(torqueGroup, SWT.NULL);
				GridDataFactory.swtDefaults().align(SWT.TRAIL, SWT.CENTER).applyTo(label);
				label.setText(Resources.get("lbl.rpm", Torque.getRpm(index)));
				label.setToolTipText(key);
				final Text text = new Text(torqueGroup, SWT.BORDER);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(text);
				final int maxFieldSize = Short.toString(Torque.MAX_VALUE).length();
				setSampleNumber(text, maxFieldSize);
				text.setTextLimit(maxFieldSize);
				text.setToolTipText(Resources.get("msg.tooltip.default", defaultValue));
				text.setData(FormProperty.KEY_DEFAULT_VALUE, defaultValue);
				text.addFocusListener(new TorquePropertyFocusListener(key, graph));
				text.addListener(SWT.Verify, new PropertyVerifyListener());
				gui.getFormProperties().put(key, new FormProperty(label, text));
			}
			tabScrolledComposite.setContent(tabComposite);
			tabScrolledComposite.setExpandVertical(true);
			tabScrolledComposite.setExpandHorizontal(true);
			tabScrolledComposite.setMinSize(tabComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			tabItem.setControl(outerComposite);
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

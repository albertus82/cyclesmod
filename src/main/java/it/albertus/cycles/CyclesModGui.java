package it.albertus.cycles;

import it.albertus.cycles.data.BikesZip;
import it.albertus.cycles.engine.CyclesModEngine;
import it.albertus.cycles.engine.InvalidPropertyException;
import it.albertus.cycles.gui.FormProperty;
import it.albertus.cycles.gui.GuiImages;
import it.albertus.cycles.gui.PropertyFocusListener;
import it.albertus.cycles.gui.PropertyFormatter;
import it.albertus.cycles.gui.PropertyVerifyListener;
import it.albertus.cycles.gui.TorqueGraph;
import it.albertus.cycles.gui.TorquePropertyFocusListener;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Setting;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Resources;
import it.albertus.util.ExceptionUtils;
import it.albertus.util.Version;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclesModGui extends CyclesModEngine {

	private static final Logger log = LoggerFactory.getLogger(CyclesModGui.class);

	private static final Point WINDOW_SIZE = new Point(980, 680);

	private final Map<String, FormProperty> formProperties = new HashMap<String, FormProperty>();
	private final Map<Bike.Type, TorqueGraph> torqueGraphs = new EnumMap<Bike.Type, TorqueGraph>(Bike.Type.class);
	private final Properties defaultProperties;

	private CyclesModGui() throws IOException {
		// Loading default properties...
		bikesInf = new BikesInf(new BikesZip().getInputStream());
		defaultProperties = new BikesCfg(bikesInf).getProperties();
	}

	public static void main(final String... args) throws IOException {
		Display display = new Display();
		final Shell shell = new CyclesModGui().createShell(display, args.length != 0 ? args[0] : null);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private Shell createShell(final Display display, final String fileName) throws IOException {
		final Shell shell = new Shell(display);
		shell.setText(Resources.get("win.title"));
		shell.setImages(GuiImages.ICONS_WHEEL);
		GridLayout shellLayout = new GridLayout();
		shellLayout.numColumns = 1;
		shell.setLayout(shellLayout);
		shell.setSize(WINDOW_SIZE);

		// Tabs...
		final TabFolder tabFolder = new TabFolder(shell, SWT.NULL);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		tabFolder.setLayout(gridLayout);
		GridData tabGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		tabFolder.setLayoutData(tabGridData);

		// Fields...
		createForm(tabFolder);

		// Buttons...
		Composite footer = new Composite(shell, SWT.NONE);
		GridLayout footerLayout = new GridLayout();
		footerLayout.numColumns = 4;
		footer.setLayout(footerLayout);
		GridData footerGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
		footer.setLayoutData(footerGridData);

		GridData buttonLayoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		buttonLayoutData.widthHint = 120;

		// Load...
		Button loadButton = new Button(footer, SWT.PUSH);
		loadButton.setText(Resources.get("btn.load"));
		loadButton.setLayoutData(buttonLayoutData);
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog openDialog = new FileDialog(shell, SWT.OPEN);
				openDialog.setFilterExtensions(new String[] { "*.INF; *.inf; *.CFG; *.cfg" });
				String fileName = openDialog.open();
				if (StringUtils.isNotBlank(fileName)) {
					load(shell, fileName, true);
				}
			}
		});

		// Save...
		Button saveButton = new Button(footer, SWT.PUSH);
		saveButton.setText(Resources.get("btn.save"));
		saveButton.setLayoutData(buttonLayoutData);
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					updateModelValues();
				}
				catch (InvalidPropertyException ipe) {
					log.error(ExceptionUtils.getLogMessage(ipe));
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
					messageBox.setText(Resources.get("msg.warning"));
					messageBox.setMessage(ExceptionUtils.getUIMessage(ipe));
					messageBox.open();
					return;
				}
				FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
				saveDialog.setFilterExtensions(new String[] { "*.INF; *.inf" });
				saveDialog.setFileName(BikesInf.FILE_NAME);
				saveDialog.setOverwrite(true);
				String fileName = saveDialog.open();

				if (StringUtils.isNotBlank(fileName)) {
					try {
						bikesInf.write(fileName);
					}
					catch (Exception e) {
						log.error(ExceptionUtils.getLogMessage(e));
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
						messageBox.setText(Resources.get("msg.warning"));
						messageBox.setMessage(Resources.get("err.file.save", ExceptionUtils.getUIMessage(e)));
						messageBox.open();
						return;
					}
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
					messageBox.setText(Resources.get("msg.completed"));
					messageBox.setMessage(Resources.get("msg.file.saved", fileName));
					messageBox.open();
				}
			}
		});

		// Reset...
		Button resetButton = new Button(footer, SWT.PUSH);
		resetButton.setText(Resources.get("btn.reset"));
		resetButton.setLayoutData(buttonLayoutData);
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				int choose = SWT.YES;
				if (bikesInf != null) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					messageBox.setText(Resources.get("msg.warning"));
					messageBox.setMessage(Resources.get("msg.reset.overwrite"));
					choose = messageBox.open();
				}
				if (choose == SWT.YES) {
					try {
						bikesInf = new BikesInf(new BikesZip().getInputStream());
						updateFormValues();
					}
					catch (Exception e) {
						log.error(ExceptionUtils.getLogMessage(e));
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
						messageBox.setText(Resources.get("msg.warning"));
						messageBox.setMessage(Resources.get("err.reset", ExceptionUtils.getUIMessage(e)));
						messageBox.open();
					}
				}
			}
		});

		// Info...
		Button infoButton = new Button(footer, SWT.PUSH);
		infoButton.setText(Resources.get("btn.info"));
		GridData infoButtonLayoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		infoButtonLayoutData.widthHint = 30;
		infoButton.setLayoutData(infoButtonLayoutData);
		infoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
				messageBox.setText(Resources.get("msg.info.title"));
				messageBox.setMessage(Resources.get("msg.info.body", Version.getInstance().getNumber(), Version.getInstance().getDate()));
				messageBox.open();
			}
		});

		if (StringUtils.isNotBlank(fileName)) {
			load(shell, fileName, false);
		}

		return shell;
	}

	private void createForm(final TabFolder tabFolder) throws IOException {
		for (Bike bike : bikesInf.getBikes()) {
			TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
			tabItem.setText(bike.getType().getDisplacement() + " cc");

			Composite tabComposite = new Composite(tabFolder, SWT.NULL);
			tabItem.setControl(tabComposite);
			GridLayout compositeGridLayout = new GridLayout();
			compositeGridLayout.numColumns = 2;
			tabComposite.setLayout(compositeGridLayout);

			// Settings
			Group settingsGroup = new Group(tabComposite, SWT.NULL);
			settingsGroup.setText(Resources.get("lbl.settings"));
			GridLayout settingsGroupGridLayout = new GridLayout();
			settingsGroupGridLayout.numColumns = 6;
			settingsGroup.setLayout(settingsGroupGridLayout);
			GridData settingsGroupGridLayoutData = new GridData();
			settingsGroupGridLayoutData.widthHint = 575;
			settingsGroup.setLayoutData(settingsGroupGridLayoutData);

			GridData gridData = new GridData();
			gridData.minimumWidth = 65;
			gridData.grabExcessHorizontalSpace = true;
			Map<Setting, Integer> settings = bike.getSettings().getValues();
			for (Setting setting : settings.keySet()) {
				String key = BikesCfg.buildPropertyKey(bike.getType(), Settings.class, setting.toString());
				String defaultValue = defaultProperties.getProperty(key);
				Label label = new Label(settingsGroup, SWT.NULL);
				label.setText(Resources.get("lbl." + setting.toString()));
				label.setToolTipText(key);
				Text text = new Text(settingsGroup, SWT.BORDER);
				text.setText(settings.get(setting).toString());
				text.setTextLimit(5);
				text.setToolTipText(Resources.get("msg.tooltip.default", defaultValue));
				text.setLayoutData(gridData);
				text.addFocusListener(new PropertyFocusListener(defaultValue));
				text.addListener(SWT.Verify, new PropertyVerifyListener());
				formProperties.put(key, new FormProperty(label, text));
			}

			// Torque graph
			TorqueGraph graph = new TorqueGraph(tabComposite, bike);
			torqueGraphs.put(bike.getType(), graph);

			// Gearbox
			Group gearboxGroup = new Group(tabComposite, SWT.NULL);
			gearboxGroup.setText(Resources.get("lbl.gearbox"));
			GridLayout gearboxGroupGridLayout = new GridLayout();
			gearboxGroupGridLayout.numColumns = 10;
			GridData gearboxGroupGridLayoutData = new GridData();
			gearboxGroupGridLayoutData.widthHint = 575;
			gearboxGroup.setLayout(gearboxGroupGridLayout);
			gearboxGroup.setLayoutData(gearboxGroupGridLayoutData);

			Gearbox gearbox = bike.getGearbox();
			int index = 0;
			gridData = new GridData();
			gridData.minimumWidth = 50;
			gridData.grabExcessHorizontalSpace = true;
			for (int ratio : gearbox.getRatios()) {
				String key = BikesCfg.buildPropertyKey(bike.getType(), Gearbox.class, index);
				String defaultValue = defaultProperties.getProperty(key);
				Label label = new Label(gearboxGroup, SWT.NULL);
				label.setText(Resources.get("lbl.gear", index != 0 ? index : "N"));
				label.setToolTipText(key);
				Text text = new Text(gearboxGroup, SWT.BORDER);
				text.setText(Integer.toString(ratio));
				text.setTextLimit(5);
				text.setToolTipText(Resources.get("msg.tooltip.default", defaultValue));
				text.setLayoutData(gridData);
				text.addFocusListener(new PropertyFocusListener(defaultValue));
				text.addListener(SWT.Verify, new PropertyVerifyListener());
				formProperties.put(key, new FormProperty(label, text));
				index++;
			}

			// Torque
			Group torqueGroup = new Group(tabComposite, SWT.NULL);
			torqueGroup.setText(Resources.get("lbl.torque"));
			GridLayout torqueGroupGridLayout = new GridLayout();
			torqueGroupGridLayout.numColumns = 18;
			torqueGroup.setLayout(torqueGroupGridLayout);
			GridData torqueGroupGridLayoutData = new GridData(SWT.FILL, SWT.TOP, true, true);
			torqueGroupGridLayoutData.horizontalSpan = 2;
			torqueGroup.setLayoutData(torqueGroupGridLayoutData);

			Torque torque = bike.getTorque();
			index = 0;
			gridData = new GridData();
			gridData.minimumWidth = 33;
			gridData.grabExcessHorizontalSpace = true;
			for (short point : torque.getCurve()) {
				String key = BikesCfg.buildPropertyKey(bike.getType(), Torque.class, index);
				String defaultValue = defaultProperties.getProperty(key);
				Label label = new Label(torqueGroup, SWT.NULL);
				label.setText(Resources.get("lbl.rpm", Torque.getRpm(index)));
				label.setToolTipText(key);
				Text text = new Text(torqueGroup, SWT.BORDER);
				text.setText(Integer.toString(point));
				text.setTextLimit(3);
				text.setToolTipText(Resources.get("msg.tooltip.default", defaultValue));
				text.setLayoutData(gridData);
				text.addFocusListener(new TorquePropertyFocusListener(defaultValue, key, graph));
				text.addListener(SWT.Verify, new PropertyVerifyListener());
				formProperties.put(key, new FormProperty(label, text));
				index++;
			}
		}
	}

	private void updateFormValues() {
		Properties properties = new BikesCfg(bikesInf).getProperties();

		// Consistency check...
		if (properties.size() != formProperties.size()) {
			throw new IllegalStateException(Resources.get("err.properties.number"));
		}

		// Update screen values...
		for (String key : formProperties.keySet()) {
			if (!properties.containsKey(key)) {
				throw new RuntimeException(Resources.get("err.property.missing", key));
			}
			Text field = formProperties.get(key).getText();
			field.setText((String) properties.get(key)); // Update field value.

			// Update font style...
			String defaultValue = (String) defaultProperties.get(key);
			PropertyFormatter.getInstance().updateFontStyle(field, defaultValue);
		}

		// Update torque graphs...
		for (Bike bike : bikesInf.getBikes()) {
			TorqueGraph graph = torqueGraphs.get(bike.getType());
			for (short i = 0; i < bike.getTorque().getCurve().length; i++) {
				graph.getValues()[i] = bike.getTorque().getCurve()[i];
			}
			graph.refresh();
		}
	}

	private void updateModelValues() {
		for (String key : formProperties.keySet()) {
			applyProperty(key, formProperties.get(key).getValue());
		}
	}

	protected void load(final Shell shell, final String fileName, final boolean successMessage) {
		try {
			if (StringUtils.endsWithIgnoreCase(fileName, ".inf")) {
				bikesInf = new BikesInf(fileName);
				updateFormValues();
				if (successMessage) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
					messageBox.setText(Resources.get("msg.completed"));
					messageBox.setMessage(Resources.get("msg.file.loaded", fileName));
					messageBox.open();
				}
			}
			else if (StringUtils.endsWithIgnoreCase(fileName, ".cfg")) {
				bikesInf = new BikesInf(new BikesZip().getInputStream());

				BikesCfg bikesCfg = new BikesCfg(fileName);
				short changesCount = 0;
				for (Object objectKey : bikesCfg.getProperties().keySet()) {
					String key = (String) objectKey;
					if (applyProperty(key, bikesCfg.getProperties().getProperty(key))) {
						changesCount++;
					}
				}
				updateFormValues();
				if (successMessage) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
					messageBox.setText(Resources.get("msg.completed"));
					messageBox.setMessage(Resources.get("msg.customizations.applied", changesCount));
					messageBox.open();
				}
			}
			else {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
				messageBox.setText(Resources.get("msg.warning"));
				messageBox.setMessage(Resources.get("err.file.invalid"));
				messageBox.open();
			}
		}
		catch (Exception e) {
			log.error(ExceptionUtils.getLogMessage(e));
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
			messageBox.setText(Resources.get("msg.warning"));
			messageBox.setMessage(Resources.get("err.file.load", ExceptionUtils.getUIMessage(e)));
			messageBox.open();
		}
	}

}

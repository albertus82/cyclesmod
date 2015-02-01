package it.albertus.cycles;

import it.albertus.cycles.data.BikesZip;
import it.albertus.cycles.engine.CyclesModEngine;
import it.albertus.cycles.engine.InvalidPropertyException;
import it.albertus.cycles.gui.FormProperty;
import it.albertus.cycles.gui.PropertyFocusListener;
import it.albertus.cycles.gui.TorqueGraph;
import it.albertus.cycles.gui.TorquePropertyFocusListener;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Setting;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;
import it.albertus.util.ExceptionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
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

public class CyclesModWin extends CyclesModEngine {

	private static final Logger log = LoggerFactory.getLogger(CyclesModWin.class);

	private static final Point WINDOW_SIZE = new Point(840, 725);

	private final Map<String, FormProperty> formProperties = new HashMap<String, FormProperty>();
	private final Map<Bike.Type, TorqueGraph> torqueGraphs = new HashMap<Bike.Type, TorqueGraph>();
	private final Properties defaultProperties;

	private CyclesModWin() throws IOException {
		// Loading default properties...
		bikesInf = new BikesInf(new BikesZip().getInputStream());
		defaultProperties = new BikesCfg(bikesInf).getProperties();
	}

	public static void main(final String... args) throws IOException {
		Display display = new Display();
		Shell shell = new CyclesModWin().createShell(display);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	private Shell createShell(final Display display) throws IOException {
		final Shell shell = new Shell(display);
		shell.setText(Messages.get("win.title"));
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
		buttonLayoutData.widthHint = 100;

		// Load...
		Button loadButton = new Button(footer, SWT.PUSH);
		loadButton.setText(Messages.get("btn.load"));
		loadButton.setLayoutData(buttonLayoutData);
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog openDialog = new FileDialog(shell, SWT.OPEN);
				openDialog.setFilterPath(".");
				openDialog.setFilterExtensions(new String[] { "*.inf; *.cfg" });
				String fileName = openDialog.open();
				if (StringUtils.isNotBlank(fileName)) {
					try {
						if ("inf".equalsIgnoreCase(StringUtils.substringAfterLast(fileName, "."))) {
							bikesInf = new BikesInf(fileName);
							updateFormValues();
							MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
							messageBox.setText(Messages.get("msg.completed"));
							messageBox.setMessage(Messages.get("msg.file.loaded", fileName));
							messageBox.open();
						}
						else if ("cfg".equalsIgnoreCase(StringUtils.substringAfterLast(fileName, "."))) {
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
							MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
							messageBox.setText(Messages.get("msg.completed"));
							messageBox.setMessage(Messages.get("msg.customizations.applied", changesCount));
							messageBox.open();
						}
						else {
							MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
							messageBox.setText(Messages.get("msg.warning"));
							messageBox.setMessage(Messages.get("err.file.invalid"));
							messageBox.open();
						}
					}
					catch (Exception e) {
						log.error(ExceptionUtils.getLogMessage(e));
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
						messageBox.setText(Messages.get("msg.warning"));
						messageBox.setMessage(Messages.get("err.file.load", ExceptionUtils.getUIMessage(e)));
						messageBox.open();
					}
				}
			}
		});

		// Save...
		Button saveButton = new Button(footer, SWT.PUSH);
		saveButton.setText(Messages.get("btn.save"));
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
					messageBox.setText(Messages.get("msg.warning"));
					messageBox.setMessage(ExceptionUtils.getUIMessage(ipe));
					messageBox.open();
					return;
				}
				FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
				saveDialog.setFilterExtensions(new String[] { "*.inf" });
				saveDialog.setFilterPath(".");
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
						messageBox.setText(Messages.get("msg.warning"));
						messageBox.setMessage(Messages.get("err.file.save", ExceptionUtils.getUIMessage(e)));
						messageBox.open();
						return;
					}
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
					messageBox.setText(Messages.get("msg.completed"));
					messageBox.setMessage(Messages.get("msg.file.saved", fileName));
					messageBox.open();
				}
			}
		});

		// Reset...
		Button resetButton = new Button(footer, SWT.PUSH);
		resetButton.setText(Messages.get("btn.reset"));
		resetButton.setLayoutData(buttonLayoutData);
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				int choose = SWT.YES;
				if (bikesInf != null) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					messageBox.setText(Messages.get("msg.warning"));
					messageBox.setMessage(Messages.get("msg.reset.overwrite"));
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
						messageBox.setText(Messages.get("msg.warning"));
						messageBox.setMessage(Messages.get("err.reset", ExceptionUtils.getUIMessage(e)));
						messageBox.open();
					}
				}
			}
		});

		// Info...
		Button infoButton = new Button(footer, SWT.PUSH);
		infoButton.setText(Messages.get("btn.info"));
		GridData infoButtonLayoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		infoButtonLayoutData.widthHint = 30;
		infoButton.setLayoutData(infoButtonLayoutData);
		infoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
				messageBox.setText(Messages.get("msg.info.title"));
				messageBox.setMessage(Messages.get("msg.info.body", version.get("version.number"), version.get("version.date")));
				messageBox.open();
			}
		});

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
			settingsGroup.setText(Messages.get("lbl.settings"));
			GridLayout settingsGroupGridLayout = new GridLayout();
			settingsGroupGridLayout.numColumns = 6;
			settingsGroup.setLayout(settingsGroupGridLayout);
			GridData settingsGroupGridLayoutData = new GridData();
			settingsGroupGridLayoutData.widthHint = 525;
			settingsGroup.setLayoutData(settingsGroupGridLayoutData);

			GridData gridData = new GridData();
			gridData.minimumWidth = 48;
			gridData.grabExcessHorizontalSpace = true;
			Map<Setting, Integer> settings = bike.getSettings().getValues();
			for (Setting setting : settings.keySet()) {
				String key = BikesCfg.buildPropertyKey(bike.getType(), Settings.class, setting.toString());
				String defaultValue = defaultProperties.getProperty(key);
				Label label = new Label(settingsGroup, SWT.NULL);
				label.setText(Messages.get("lbl." + setting.toString()));
				label.setToolTipText(key);
				Text text = new Text(settingsGroup, SWT.BORDER);
				text.setText(settings.get(setting).toString());
				text.setTextLimit(5);
				text.setToolTipText(Messages.get("msg.tooltip.default", defaultValue));
				text.setLayoutData(gridData);
				text.addFocusListener(new PropertyFocusListener(defaultValue));
				formProperties.put(key, new FormProperty(label, text));
			}

			// Torque graph
			Canvas graphCanvas = new Canvas(tabComposite, SWT.NULL);
			GridData graphGridLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
			graphGridLayoutData.verticalSpan = 2;
			graphCanvas.setLayoutData(graphGridLayoutData);

			final LightweightSystem lws = new LightweightSystem(graphCanvas);

			XYGraph xyGraph = new XYGraph();
			xyGraph.setTitle(Messages.get("lbl.graph.title"));
			lws.setContents(xyGraph);

			xyGraph.primaryXAxis.setShowMajorGrid(true);
			xyGraph.primaryYAxis.setShowMajorGrid(true);

			double[] x = new double[Torque.LENGTH], y = new double[Torque.LENGTH];

			for (short i = 0; i < bike.getTorque().getCurve().length; i++) {
				x[i] = ((double) Torque.getRpm(i)) / 1000;
				y[i] = bike.getTorque().getCurve()[i];
			}

			CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);
			traceDataProvider.setBufferSize(x.length);
			traceDataProvider.setCurrentXDataArray(x);
			traceDataProvider.setCurrentYDataArray(y);

			Font sysFont = Display.getCurrent().getSystemFont();
			
			Axis abscissae = xyGraph.primaryXAxis;
			abscissae.setAutoScale(true);
			abscissae.setTitle(Messages.get("lbl.graph.axis.x"));
			abscissae.setTitleFont(XYGraphMediaFactory.getInstance().getFont(new FontData(sysFont.getFontData()[0].getName(), 9, SWT.BOLD)));

			Axis ordinates = xyGraph.primaryYAxis;
			ordinates.setAutoScale(true);
			ordinates.setTitle(Messages.get("lbl.graph.axis.y"));
			ordinates.setTitleFont(XYGraphMediaFactory.getInstance().getFont(new FontData(sysFont.getFontData()[0].getName(), 9, SWT.BOLD)));

			Trace trace = new Trace("Torque", abscissae, ordinates, traceDataProvider);
			trace.setPointStyle(PointStyle.NONE);
			trace.setLineWidth(3);

			xyGraph.addTrace(trace);
			xyGraph.setShowLegend(false);
			xyGraph.setTitleFont(XYGraphMediaFactory.getInstance().getFont(new FontData(sysFont.getFontData()[0].getName(), 11, SWT.BOLD)));

			TorqueGraph graph = new TorqueGraph(trace, y);
			torqueGraphs.put(bike.getType(), graph);

			// Gearbox
			Group gearboxGroup = new Group(tabComposite, SWT.NULL);
			gearboxGroup.setText(Messages.get("lbl.gearbox"));
			GridLayout gearboxGroupGridLayout = new GridLayout();
			gearboxGroupGridLayout.numColumns = 10;
			GridData gearboxGroupGridLayoutData = new GridData();
			gearboxGroupGridLayoutData.widthHint = 525;
			gearboxGroup.setLayout(gearboxGroupGridLayout);
			gearboxGroup.setLayoutData(gearboxGroupGridLayoutData);

			Gearbox gearbox = bike.getGearbox();
			int index = 0;
			gridData = new GridData();
			gridData.minimumWidth = 48;
			gridData.grabExcessHorizontalSpace = true;
			for (int ratio : gearbox.getRatios()) {
				String key = BikesCfg.buildPropertyKey(bike.getType(), Gearbox.class, index);
				String defaultValue = defaultProperties.getProperty(key);
				Label label = new Label(gearboxGroup, SWT.NULL);
				label.setText(Messages.get("lbl.gear", index != 0 ? index : "N"));
				label.setToolTipText(key);
				Text text = new Text(gearboxGroup, SWT.BORDER);
				text.setText(Integer.toString(ratio));
				text.setTextLimit(5);
				text.setToolTipText(Messages.get("msg.tooltip.default", defaultValue));
				text.setLayoutData(gridData);
				text.addFocusListener(new PropertyFocusListener(defaultValue));
				formProperties.put(key, new FormProperty(label, text));
				index++;
			}

			// Torque
			Group torqueGroup = new Group(tabComposite, SWT.NULL);
			torqueGroup.setText(Messages.get("lbl.torque"));
			GridLayout torqueGroupGridLayout = new GridLayout();
			torqueGroupGridLayout.numColumns = 16;
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
				label.setText(Messages.get("lbl.rpm", Torque.getRpm(index)));
				label.setToolTipText(key);
				Text text = new Text(torqueGroup, SWT.BORDER);
				text.setText(Integer.toString(point));
				text.setTextLimit(3);
				text.setToolTipText(Messages.get("msg.tooltip.default", defaultValue));
				text.setLayoutData(gridData);
				text.addFocusListener(new TorquePropertyFocusListener(defaultValue, key, graph));
				formProperties.put(key, new FormProperty(label, text));
				index++;
			}
		}
	}

	private void updateFormValues() {
		Properties properties = new BikesCfg(bikesInf).getProperties();

		// Consistency check...
		if (properties.size() != formProperties.size()) {
			throw new IllegalStateException(Messages.get("err.properties.number"));
		}

		// Update screen values...
		for (String key : formProperties.keySet()) {
			if (!properties.containsKey(key)) {
				throw new RuntimeException(Messages.get("err.property.missing", key));
			}
			Text field = formProperties.get(key).getText();
			field.setText((String) properties.get(key)); // Update field value.

			// Update font style...
			String defaultValue = (String) defaultProperties.get(key);
			PropertyFocusListener.updateFontStyle(field, defaultValue);
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

}
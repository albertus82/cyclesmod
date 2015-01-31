package it.albertus.cycles.gui;

import it.albertus.cycles.data.BikesZip;
import it.albertus.cycles.engine.CyclesModEngine;
import it.albertus.cycles.engine.InvalidPropertyException;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Setting;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

public class CyclesModGUI extends CyclesModEngine {

	private static final Logger log = LoggerFactory.getLogger(CyclesModGUI.class);

	private Map<String, FormProperty> formProperties = new HashMap<String, FormProperty>();
	private Properties defaultProperties;

	public static void main(String[] args) throws IOException {
		Display display = new Display();
		Shell shell = new CyclesModGUI().createShell(display);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	public Shell createShell(final Display display) throws IOException {
		final Shell shell = new Shell(display);
		shell.setText(Messages.get("win.title"));
		GridLayout shellLayout = new GridLayout();
		shellLayout.numColumns = 1;
		shell.setLayout(shellLayout);
		shell.setSize(830, 680);

		// Tab
		final TabFolder tabFolder = new TabFolder(shell, SWT.BORDER);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		tabFolder.setLayout(gridLayout);
		GridData tabGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		tabFolder.setLayoutData(tabGridData);

		createForm(tabFolder);

		// Buttons...
		Composite footer = new Composite(shell, SWT.NONE);
		GridLayout footerLayout = new GridLayout();
		footerLayout.numColumns = 3;
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
						}
						else if ("cfg".equalsIgnoreCase(StringUtils.substringAfterLast(fileName, "."))) {
							bikesInf = new BikesInf(new BikesZip().getInputStream()); // Load
																						// defaults.
							BikesCfg bikesCfg = new BikesCfg(fileName);
							short changesCount = 0;
							for (Object objectKey : bikesCfg.getProperties().keySet()) {
								String key = (String) objectKey;
								if (applyProperty(key, bikesCfg.getProperties().getProperty(key))) {
									changesCount++;
								}
							}
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
							return;
						}
						updateFormValues();
					}
					catch (Exception e) {
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
						messageBox.setText(Messages.get("msg.warning"));
						StringBuilder message = new StringBuilder(Messages.get("err.generic"));
						if (StringUtils.isNotBlank(e.getLocalizedMessage())) {
							message.append(' ').append(e.getLocalizedMessage());
						}
						messageBox.setMessage(message.toString());
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
			public void widgetSelected(SelectionEvent e) {
				if (bikesInf == null) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING);
					messageBox.setText("Attenzione!");
					messageBox.setMessage("Non ci sono dati da salvare. Caricare prima un file BIKES.INF valido.");
					messageBox.open();
				}
				else {
					try {
						updateModelValues();
					}
					catch (InvalidPropertyException ipe) {
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
						messageBox.setText("Attenzione!");
						messageBox.setMessage(ipe.getMessage());
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
						catch (IOException e1) {
							e1.printStackTrace(); // TODO
						}
					}
				}
			}
		});

		// Reset...
		Button resetButton = new Button(footer, SWT.PUSH);
		resetButton.setText(Messages.get("btn.reset"));
		resetButton.setLayoutData(buttonLayoutData);
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int choose = SWT.YES;
				if (bikesInf != null) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					messageBox.setText("Attenzione!");
					messageBox.setMessage("Sovrascrivere i valori correnti con quelli predefiniti?");
					choose = messageBox.open();
				}
				if (choose == SWT.YES) {
					try {
						bikesInf = new BikesInf(new BikesZip().getInputStream());
						log.debug("Defaults loaded!");
						updateFormValues();
					}
					catch (IOException e1) {
						e1.printStackTrace(); // TODO
					}
				}
			}
		});

		return shell;
	}

	private void createForm(final TabFolder tabFolder) throws IOException {
		bikesInf = new BikesInf(new BikesZip().getInputStream());
		defaultProperties = new BikesCfg(bikesInf).getProperties();

		for (Bike.Type bikeType : Bike.Type.values()) {
			TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
			tabItem.setText(bikeType.getDisplacement() + " cc");

			Composite tabComposite = new Composite(tabFolder, SWT.NULL);
			tabItem.setControl(tabComposite);
			GridLayout compositeGridLayout = new GridLayout();
			compositeGridLayout.numColumns = 1;
			tabComposite.setLayout(compositeGridLayout);

			// Inserire qui tutti i controlli di ogni tab
			Bike bike = bikesInf.getBike(bikeType.getDisplacement());

			Group settingsGroup = new Group(tabComposite, SWT.NULL);
			settingsGroup.setText(Messages.get("lbl.settings"));
			GridLayout settingsGroupGridLayout = new GridLayout();
			settingsGroupGridLayout.numColumns = 12;
			settingsGroup.setLayout(settingsGroupGridLayout);

			Group gearboxGroup = new Group(tabComposite, SWT.NULL);
			gearboxGroup.setText(Messages.get("lbl.gearbox"));
			GridLayout gearboxGroupGridLayout = new GridLayout();
			gearboxGroupGridLayout.numColumns = 10;
			gearboxGroup.setLayout(gearboxGroupGridLayout);

			Group torqueGroup = new Group(tabComposite, SWT.NULL);
			torqueGroup.setText(Messages.get("lbl.torque"));
			GridLayout torqueGroupGridLayout = new GridLayout();
			torqueGroupGridLayout.numColumns = 16;
			torqueGroup.setLayout(torqueGroupGridLayout);

			// Settings
			GridData gridData = new GridData();
			gridData.minimumWidth = 42;
			gridData.grabExcessHorizontalSpace = true;
			Map<Setting, Integer> settings = bike.getSettings().getValues();
			for (Setting setting : settings.keySet()) {
				String key = BikesCfg.buildPropertyKey(bikeType, Settings.class, setting.toString());
				Label label = new Label(settingsGroup, SWT.NULL);
				label.setText(setting.toString());
				label.setToolTipText(key);
				Text text = new Text(settingsGroup, SWT.BORDER);
				text.setText(settings.get(setting).toString());
				text.setTextLimit(5);
				text.setToolTipText(Messages.get("msg.tooltip.default", defaultProperties.getProperty(key)));
				text.setLayoutData(gridData);
				formProperties.put(key, new FormProperty(label, text));
			}

			// Gearbox
			Gearbox gearbox = bike.getGearbox();
			int index = 0;
			gridData = new GridData();
			gridData.minimumWidth = 42;
			gridData.grabExcessHorizontalSpace = true;
			for (int ratio : gearbox.getRatios()) {
				String key = BikesCfg.buildPropertyKey(bikeType, Gearbox.class, index);
				Label label = new Label(gearboxGroup, SWT.NULL);
				label.setText(Messages.get("lbl.gear", index != 0 ? index : "N"));
				label.setToolTipText(key);
				Text text = new Text(gearboxGroup, SWT.BORDER);
				text.setText(Integer.toString(ratio));
				text.setTextLimit(5);
				text.setToolTipText(Messages.get("msg.tooltip.default", defaultProperties.getProperty(key)));
				text.setLayoutData(gridData);
				formProperties.put(key, new FormProperty(label, text));
				index++;
			}

			// Torque
			Torque torque = bike.getTorque();
			index = 0;
			gridData = new GridData();
			gridData.minimumWidth = 30;
			gridData.grabExcessHorizontalSpace = true;
			for (int point : torque.getCurve()) {
				String key = BikesCfg.buildPropertyKey(bikeType, Torque.class, index);
				Label label = new Label(torqueGroup, SWT.NULL);
				label.setText(Messages.get("lbl.rpm", Torque.getRpm(index)));
				label.setToolTipText(key);
				Text text = new Text(torqueGroup, SWT.BORDER);
				text.setText(Integer.toString(point));
				text.setTextLimit(3);
				text.setToolTipText(Messages.get("msg.tooltip.default", defaultProperties.getProperty(key)));
				text.setLayoutData(gridData);
				formProperties.put(key, new FormProperty(label, text));
				index++;
			}
		}
	}

	private void updateFormValues() {
		Properties properties = new BikesCfg(bikesInf).getProperties();

		// Consistency check...
		if (properties.size() != formProperties.size()) {
			throw new IllegalStateException("Numerosita' properties inconsistente");
		}

		// Update screen values...
		for (String key : formProperties.keySet()) {
			if (!properties.containsKey(key)) {
				throw new RuntimeException("Property non presente: " + key);
			}
			formProperties.get(key).getText().setText((String) properties.get(key));
		}
	}

	private void updateModelValues() {
		for (String key : formProperties.keySet()) {
			applyProperty(key, formProperties.get(key).getValue());
		}
	}

}
package it.albertus.cycles.gui;

import it.albertus.cycles.data.BikesZip;
import it.albertus.cycles.engine.InvalidPropertyException;
import it.albertus.cycles.engine.PropertyParser;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Setting;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;

import java.beans.Introspector;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

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

public class CyclesModW extends PropertyParser {

	private static final Logger log = LoggerFactory.getLogger(CyclesModW.class);

	private Map<String, FormProperty> formProperties = new TreeMap<String, FormProperty>();

	public static void main(String[] args) throws IOException {
		Display display = new Display();
		Shell shell = new CyclesModW().createShell(display);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	public Shell createShell(final Display display) throws IOException {
		final Shell shell = new Shell(display);
		shell.setText("CyclesMod");
		GridLayout shellLayout = new GridLayout();
		shellLayout.numColumns = 1;
		shell.setLayout(shellLayout);

		// Tab
		final TabFolder tabFolder = new TabFolder(shell, SWT.BORDER);
		// tabFolder.setSize(750, 500);
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

		// Load...
		Button loadButton = new Button(footer, SWT.PUSH);
		loadButton.setText("Load");
		loadButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog openDialog = new FileDialog(shell, SWT.OPEN);
				openDialog.setFilterPath(".");
				openDialog.setFilterExtensions(new String[] { "*.inf" });
				String fileName = openDialog.open();
				if (StringUtils.isNotBlank(fileName)) {
					try {
						bikesInf = new BikesInf(fileName);
						log.info("Loaded!");
						updateForm();
					} catch (IOException e1) {
						e1.printStackTrace(); // TODO
					}
				}
			}
		});

		// Save...
		Button saveButton = new Button(footer, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (bikesInf == null) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING);
					messageBox.setText("Attenzione!");
					messageBox.setMessage("Non ci sono dati da salvare. Caricare prima un file BIKES.INF valido.");
					messageBox.open();
				} else {
					try {
						updateModel();
					}
					catch ( InvalidPropertyException ipe ) {
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
						messageBox.setText("Attenzione!");
						messageBox.setMessage( ipe.getMessage() );
						messageBox.open();
						return;
					}
					FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
					saveDialog.setFilterExtensions(new String[] { "*.inf" });
					saveDialog.setFilterPath(".");
					saveDialog.setFileName("BIKES.INF");
					saveDialog.setOverwrite(true);
					String fileName = saveDialog.open();

					if (StringUtils.isNotBlank(fileName)) {
						try {
							bikesInf.write(fileName);
							log.debug("Saved!");
						} catch (IOException e1) {
							e1.printStackTrace(); // TODO
						}
					}
				}
			}
		});

		// Reset...
		Button resetButton = new Button(footer, SWT.PUSH);
		resetButton.setText("Reset");
		resetButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
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
						updateForm();
					} catch (IOException e1) {
						e1.printStackTrace(); // TODO

					}
				}
			}
		});

		shell.pack();

		return shell;
	}

	private void createForm(final TabFolder tabFolder) throws IOException {
		bikesInf = new BikesInf(new BikesZip().getInputStream());
		BikesCfg bikesCfg = new BikesCfg( bikesInf ); 
		Properties properties = bikesCfg.getProperties();
		
		Map<String, String> sortedProperties = new TreeMap<String, String>();
		for (final String key: properties.stringPropertyNames()) {
			sortedProperties.put(key, properties.getProperty(key));
		}

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
			String prefix = Integer.toString(bike.getType().getDisplacement());

			// Settings
			{
				Group settingsGroup = new Group(tabComposite, SWT.NULL);
				settingsGroup.setText("Settings");
				GridLayout settingsGroupGridLayout = new GridLayout();
				settingsGroupGridLayout.numColumns = 12;
				settingsGroup.setLayout(settingsGroupGridLayout);

				Map<Setting, Integer> settings = bike.getSettings().getValues();
				for (Setting setting : settings.keySet()) {
					String key = prefix + '.' + Introspector.decapitalize(Settings.class.getSimpleName()) + '.' + setting.toString(); // TODO esternalizzare il pattern
					Label label = new Label(settingsGroup, SWT.NULL);
					label.setText(setting.toString());
					Text text = new Text(settingsGroup, SWT.BORDER);
					text.setText( settings.get(setting).toString());
					text.setTextLimit(5);
					formProperties.put(key, new FormProperty(label, text));
				}
			}

			// Gearbox
			{
				Group gearboxGroup = new Group(tabComposite, SWT.NULL);
				gearboxGroup.setText("Gearbox");
				GridLayout gearboxGroupGridLayout = new GridLayout();
				gearboxGroupGridLayout.numColumns = 10;
				gearboxGroup.setLayout(gearboxGroupGridLayout);

				Gearbox gearbox = bike.getGearbox();
				int index = 0;
				for (int ratio : gearbox.getRatios()) {
					String key = prefix + '.' + Introspector.decapitalize(Gearbox.class.getSimpleName()) + '.' + index; // TODO esternalizzare il pattern
					Label label = new Label(gearboxGroup, SWT.NULL);
					label.setText("Gear " + index);
					Text text = new Text(gearboxGroup, SWT.BORDER);
					text.setText(Integer.toString(ratio));
					text.setTextLimit(5);
					formProperties.put(key, new FormProperty(label, text));
					index++;
				}
			}

			// Torque
			{
				Group torqueGroup = new Group(tabComposite, SWT.NULL);
				torqueGroup.setText("Torque");
				GridLayout torqueGroupGridLayout = new GridLayout();
				torqueGroupGridLayout.numColumns = 16;
				torqueGroup.setLayout(torqueGroupGridLayout);

				Torque torque = bike.getTorque();
				int index = 0;
				for (int point : torque.getCurve()) {
					String key = prefix + '.' + Introspector.decapitalize(Torque.class.getSimpleName()) + '.' + index; // TODO esternalizzare il pattern
					Label label = new Label(torqueGroup, SWT.NULL);
					label.setText(Torque.getRpm(index) + " RPM");
					Text text = new Text(torqueGroup, SWT.BORDER);
					text.setText(Integer.toString(point));
					text.setTextLimit(3);
					formProperties.put(key, new FormProperty(label, text));
					index++;
				}
			}
		}
	}
	
	private void updateForm() {
		Properties properties = new BikesCfg(bikesInf).getProperties();
		
		// Consistency check...
		if( properties.size() != formProperties.size() ) {
			throw new IllegalStateException("Numerosita' properties inconsistente");
		}
		
		// Update screen values...
		for ( String key : formProperties.keySet() ) {
			if ( !properties.containsKey( key ) ) {
				throw new RuntimeException("Property non presente: " + key );
			}
			formProperties.get(key).getText().setText( (String) properties.get(key) );
		}
	}
	
	private void updateModel() {
		for ( String key : formProperties.keySet() ) {
			applyProperty( key, formProperties.get( key ).getValue() );
		}
	}


}
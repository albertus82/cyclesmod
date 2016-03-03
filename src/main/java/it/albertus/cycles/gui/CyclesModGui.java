package it.albertus.cycles.gui;

import it.albertus.cycles.data.BikesZip;
import it.albertus.cycles.engine.CyclesModEngine;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.model.Gearbox;
import it.albertus.cycles.model.Setting;
import it.albertus.cycles.model.Settings;
import it.albertus.cycles.model.Torque;
import it.albertus.cycles.resources.Resources;
import it.albertus.util.ExceptionUtils;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class CyclesModGui extends CyclesModEngine implements Gui {

	private final Map<String, FormProperty> formProperties = new HashMap<String, FormProperty>();
	private final Map<Bike.Type, TorqueGraph> torqueGraphs = new EnumMap<Bike.Type, TorqueGraph>(Bike.Type.class);
	private final Properties defaultProperties;

	private Shell shell;
	private Menu menuBar;
	private Menu fileMenu;
	private MenuItem fileMenuHeader;
	private Menu helpMenu;
	private MenuItem helpMenuHeader;
	private MenuItem fileExitMenuItem;
	private MenuItem helpAboutMenuItem;
	private MenuItem fileOpenMenuItem;
	private Menu editMenu;
	private MenuItem editMenuHeader;
	private MenuItem editResetMenuItem;
	private TabFolder tabFolder;
	private MenuItem editFullResetMenuItem;
	private MenuItem editSingleResetMenuItem;
	private Menu editResetMenu;
	private MenuItem editResetSubMenuItem;
	private Menu editResetSubMenu;
	private MenuItem editResetSingleMenuItem;
	private MenuItem editResetAllMenuItem;

	private CyclesModGui() throws IOException {
		// Loading default properties...
		setBikesInf(new BikesInf(new BikesZip().getInputStream()));
		defaultProperties = new BikesCfg(getBikesInf()).getProperties();
	}

	/* Entry point */
	public static void start(final String fileName) throws IOException {
		Display display = new Display();
		final CyclesModGui gui = new CyclesModGui();
		final Shell shell = gui.createShell(display);

		// Loading custom properties...
		if (StringUtils.isNotBlank(fileName)) {
			gui.load(fileName, false);
		}

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private Shell createShell(final Display display) throws IOException {
		shell = new Shell(display);
		shell.setImages(Images.ICONS_TOOLS);
		shell.setText(Resources.get("win.title"));
		shell.setLayout(new FillLayout());
		shell.addListener(SWT.Close, new CloseListener(this));

		createMenuBar();

		// Tabs...
		tabFolder = new TabFolder(shell, SWT.NULL);
		// TODO Aggiungere scrollbar verticale!

		// Fields...
		createForm(tabFolder);

		// Size...
		shell.pack();
		shell.setMinimumSize(shell.getSize());

		return shell;
	}

	private void createMenuBar() {
		menuBar = new Menu(shell, SWT.BAR); // Barra

		// File
		fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText(Resources.get("lbl.menu.header.file"));
		fileMenuHeader.setMenu(fileMenu);

		fileOpenMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenMenuItem.setText(Resources.get("lbl.menu.item.open"));
		fileOpenMenuItem.addSelectionListener(new OpenSelectionListener(this));

		fileOpenMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenMenuItem.setText(Resources.get("lbl.menu.item.saveas"));
		fileOpenMenuItem.addSelectionListener(new SaveSelectionListener(this));

		new MenuItem(fileMenu, SWT.SEPARATOR);

		fileExitMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitMenuItem.setText(Resources.get("lbl.menu.item.exit"));
		fileExitMenuItem.addSelectionListener(new CloseListener(this));

		// Edit
		editMenu = new Menu(shell, SWT.DROP_DOWN);
		editMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		editMenuHeader.setText(Resources.get("lbl.menu.header.edit"));
		editMenuHeader.setMenu(editMenu);

		editResetSubMenuItem = new MenuItem(editMenu, SWT.CASCADE);
		editResetSubMenuItem.setText(Resources.get("lbl.menu.item.reset"));

		editResetSubMenu = new Menu(shell, SWT.DROP_DOWN);
		editResetSubMenuItem.setMenu(editResetSubMenu);

		editResetSingleMenuItem = new MenuItem(editResetSubMenu, SWT.PUSH);
		editResetSingleMenuItem.setText(Resources.get("lbl.menu.item.reset.single"));
		editResetSingleMenuItem.addSelectionListener(new ResetSingleSelectionListener(this));

		editResetAllMenuItem = new MenuItem(editResetSubMenu, SWT.PUSH);
		editResetAllMenuItem.setText(Resources.get("lbl.menu.item.reset.all"));
		editResetAllMenuItem.addSelectionListener(new ResetAllSelectionListener(this));

		// Help
		helpMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		helpMenuHeader.setText(Resources.get("lbl.menu.header.help"));
		helpMenuHeader.setMenu(helpMenu);

		helpAboutMenuItem = new MenuItem(helpMenu, SWT.PUSH);
		helpAboutMenuItem.setText(Resources.get("lbl.menu.item.about"));
		helpAboutMenuItem.addSelectionListener(new AboutSelectionListener(this));

		shell.setMenuBar(menuBar);
	}

	private void createForm(final TabFolder tabFolder) throws IOException {
		for (Bike bike : getBikesInf().getBikes()) {
			TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
			tabItem.setText(bike.getType().getDisplacement() + " cc");

			Composite tabComposite = new Composite(tabFolder, SWT.NULL);
			tabItem.setControl(tabComposite);
			GridLayout compositeGridLayout = new GridLayout(2, false);
			tabComposite.setLayout(compositeGridLayout);

			// Settings
			Group settingsGroup = new Group(tabComposite, SWT.NULL);
			settingsGroup.setText(Resources.get("lbl.settings"));
			// Posizionamento dell'elemento all'interno del contenitore
			GridData settingsGroupGridLayoutData = new GridData(GridData.FILL, GridData.FILL, false, true);
			settingsGroup.setLayoutData(settingsGroupGridLayoutData);
			// Definizione di come saranno disposti gli elementi contenuti
			GridLayout settingsGroupGridLayout = new GridLayout();
			settingsGroupGridLayout.numColumns = 6;
			settingsGroup.setLayout(settingsGroupGridLayout);

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
			gearboxGroup.setLayout(gearboxGroupGridLayout);
			GridData gearboxGroupGridLayoutData = new GridData(GridData.FILL, GridData.FILL, false, true);
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
			GridData torqueGroupGridLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
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

	public void updateFormValues() {
		Properties properties = new BikesCfg(getBikesInf()).getProperties();

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
		for (Bike bike : getBikesInf().getBikes()) {
			TorqueGraph graph = torqueGraphs.get(bike.getType());
			for (short i = 0; i < bike.getTorque().getCurve().length; i++) {
				graph.getValues()[i] = bike.getTorque().getCurve()[i];
			}
			graph.refresh();
		}
	}

	public void updateModelValues(boolean lenient) {
		for (String key : formProperties.keySet()) {
			applyProperty(key, formProperties.get(key).getValue(), lenient);
		}
	}

	public void load(final String fileName, final boolean successMessage) {
		try {
			if (StringUtils.endsWithIgnoreCase(fileName, ".inf")) {
				setBikesInf(new BikesInf(fileName));
				updateFormValues();
				if (successMessage) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
					messageBox.setText(Resources.get("msg.completed"));
					messageBox.setMessage(Resources.get("msg.file.loaded", fileName));
					messageBox.open();
				}
			}
			else if (StringUtils.endsWithIgnoreCase(fileName, ".cfg")) {
				setBikesInf(new BikesInf(new BikesZip().getInputStream()));

				BikesCfg bikesCfg = new BikesCfg(fileName);
				short changesCount = 0;
				for (Object objectKey : bikesCfg.getProperties().keySet()) {
					String key = (String) objectKey;
					if (applyProperty(key, bikesCfg.getProperties().getProperty(key), false)) {
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
			System.err.println(ExceptionUtils.getLogMessage(e));
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
			messageBox.setText(Resources.get("msg.warning"));
			messageBox.setMessage(Resources.get("err.file.load", ExceptionUtils.getUIMessage(e)));
			messageBox.open();
		}
	}

	@Override
	public Shell getShell() {
		return shell;
	}

	public Map<String, FormProperty> getFormProperties() {
		return formProperties;
	}

	public Map<Bike.Type, TorqueGraph> getTorqueGraphs() {
		return torqueGraphs;
	}

	public Properties getDefaultProperties() {
		return defaultProperties;
	}

	public Menu getMenuBar() {
		return menuBar;
	}

	public Menu getFileMenu() {
		return fileMenu;
	}

	public MenuItem getFileMenuHeader() {
		return fileMenuHeader;
	}

	public Menu getHelpMenu() {
		return helpMenu;
	}

	public MenuItem getHelpMenuHeader() {
		return helpMenuHeader;
	}

	public MenuItem getFileExitMenuItem() {
		return fileExitMenuItem;
	}

	public MenuItem getHelpAboutMenuItem() {
		return helpAboutMenuItem;
	}

	public MenuItem getFileOpenMenuItem() {
		return fileOpenMenuItem;
	}

	public Menu getEditMenu() {
		return editMenu;
	}

	public MenuItem getEditMenuHeader() {
		return editMenuHeader;
	}

	public MenuItem getEditResetMenuItem() {
		return editResetMenuItem;
	}

	public TabFolder getTabFolder() {
		return tabFolder;
	}

	public MenuItem getEditFullResetMenuItem() {
		return editFullResetMenuItem;
	}

	public MenuItem getEditSingleResetMenuItem() {
		return editSingleResetMenuItem;
	}

	public Menu getEditResetMenu() {
		return editResetMenu;
	}

	public MenuItem getEditResetSubMenuItem() {
		return editResetSubMenuItem;
	}

	public Menu getEditResetSubMenu() {
		return editResetSubMenu;
	}

	public MenuItem getEditResetSingleMenuItem() {
		return editResetSingleMenuItem;
	}

	public MenuItem getEditResetAllMenuItem() {
		return editResetAllMenuItem;
	}

}

package it.albertus.cycles.gui;

import it.albertus.cycles.data.BikesZip;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesInf;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclesModW {

	private static final Logger log = LoggerFactory.getLogger(CyclesModW.class);

	private BikesInf bikesInf;
	
	private Map<Bike.Type, Map<String, Control>> controls;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new CyclesModW().createShell(display);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	private void populateControls() {
		controls = new TreeMap<Bike.Type, Map<String, Control>>();
//		if ( bikesInf != null ) {
//			for ( Bike bike : bikesInf.getBikes() ) {
//				Settings settings = bike.getSettings();
//				for ( Setting setting : settings.getValues().keySet() ) {
//					settings.getValues().get( setting );
//				}
//			}
//		}
	}

	public Shell createShell(final Display display) {
		populateControls();

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

		
		for (Bike.Type bikeType : Bike.Type.values()) {
			TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
			tabItem.setText(bikeType.getDisplacement() + " cc");

			Composite tabComposite = new Composite(tabFolder, SWT.NULL);
			tabItem.setControl(tabComposite);
			GridLayout compositeGridLayout = new GridLayout();
			compositeGridLayout.numColumns = 6;
			tabComposite.setLayout(compositeGridLayout);

			// Inserire qui tutti i controlli di ogni tab
			Text text = new Text(tabComposite, SWT.BORDER);
			text.setText("This is page " + bikeType.toString());
			
			
			Map<String, Control> map = new TreeMap<String, Control>();
			
			Text text1 = new Text(tabFolder, SWT.BORDER);
			text1.setText( "moto " + bikeType.toString() );
			map.put( "ciao", text1 ); 
			controls.put(bikeType, map);
//			tabItem.setControl( text1 );
		}
		
		
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
					} catch (IOException e1) {
						e1.printStackTrace(); // TODO

					}
				}
			}
		});

		shell.pack();

		return shell;
	}


}
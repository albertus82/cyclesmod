package it.albertus.cycles.gui;

import it.albertus.cycles.data.BikesZip;
import it.albertus.cycles.model.Bike;
import it.albertus.cycles.model.BikesInf;

import java.io.IOException;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class CyclesModW {

	private BikesInf bikesInf;
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new CyclesModW().createShell(display);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	public Shell createShell(final Display display) {
		final Shell shell = new Shell(display);
		shell.setText("CyclesMod");
		GridLayout shellLayout = new GridLayout();
		shellLayout.numColumns = 1;
		shell.setLayout( shellLayout );
		
		// Tab
		final TabFolder tabFolder = new TabFolder(shell, SWT.BORDER);
//		tabFolder.setSize(750, 500);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		tabFolder.setLayout(gridLayout);
		GridData tabGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		tabFolder.setLayoutData(tabGridData);

		for (Bike.Type bikeType : Bike.Type.values()) {
			TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
			tabItem.setText(bikeType.getDisplacement() + " cc");

			Composite tabComposite = new Composite( tabFolder, SWT.NULL);
			tabItem.setControl(tabComposite);
			tabComposite.setLayout( new GridLayout());

			// Inserire qui tutti i controlli di ogni tab
			Text text = new Text(tabComposite, SWT.BORDER);
			text.setText("This is page " + bikeType.toString());
			
		}
		
		// Load/Save buttons...
		Composite footer = new Composite( shell, SWT.NONE );
		GridLayout footerLayout = new GridLayout();
		footerLayout.numColumns = 2;
		footer.setLayout(gridLayout);
		GridData footerGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
		footerGridData.horizontalSpan = 2;
		footer.setLayoutData(footerGridData);
		
		// Load...
		Button loadButton = new Button( footer, SWT.PUSH);
		loadButton.setText( "Load" );
		loadButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog openDialog = new FileDialog(shell, SWT.OPEN );
				openDialog.setFilterExtensions( new String[] {"*.inf"} );
				String fileName = openDialog.open();
				if ( StringUtils.isNotBlank( fileName ) ) {
					try {
						bikesInf = new BikesInf( fileName );
						System.out.println( "LOADED!!!");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		// Save...
		Button saveButton = new Button( footer, SWT.PUSH);
		saveButton.setText( "Save" );
		saveButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if ( bikesInf == null ) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING );
					messageBox.setText("Attenzione!");
				    messageBox.setMessage("Non ci sono dati da salvare. Caricare prima un file BIKES.INF valido.");
				    messageBox.open();
				}
				else {
					FileDialog saveDialog = new FileDialog(shell, SWT.SAVE );
					saveDialog.setFilterExtensions( new String[] {"*.inf"} );
					saveDialog.setOverwrite( true );
					String fileName = saveDialog.open();
					
					if ( StringUtils.isNotBlank( fileName ) ) {
						try {
							bikesInf.write( fileName );
							System.out.println( "SAVED!!!" );
						} catch (IOException e1) {
							e1.printStackTrace();
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
				try {
					bikesInf = new BikesInf(new BikesZip().getInputStream());
					System.out.println("Defaults loaded!!!");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});


		shell.pack();

		return shell;
	}
	
}
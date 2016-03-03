package it.albertus.cycles.gui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ExitSelectionListener extends SelectionAdapter {

	private final Gui gui;

	public ExitSelectionListener(Gui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		gui.getShell().close();
	}

}

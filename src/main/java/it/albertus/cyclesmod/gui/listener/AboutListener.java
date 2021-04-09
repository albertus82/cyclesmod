package it.albertus.cyclesmod.gui.listener;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import it.albertus.cyclesmod.gui.AboutDialog;

public class AboutListener extends SelectionAdapter implements Listener {

	private final IShellProvider gui;

	public AboutListener(final IShellProvider gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		new AboutDialog(gui.getShell()).open();
	}

	@Override
	public void handleEvent(final Event event) {
		widgetSelected(null);
	}

}

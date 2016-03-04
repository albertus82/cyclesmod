package it.albertus.cycles.gui;

import it.albertus.cycles.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;

public class CloseListener extends SelectionAdapter implements Listener {

	private final Gui gui;

	public CloseListener(Gui gui) {
		this.gui = gui;
	}

	/** Comando di chiusura da men&ugrave;. */
	@Override
	public void widgetSelected(SelectionEvent e) {
		if (confirmClose()) {
			gui.getShell().dispose();
		}
	}

	/** Pulsante di chiusura. */
	@Override
	public void handleEvent(Event event) {
		event.doit = confirmClose();
	}

	private boolean confirmClose() {
		final MessageBox messageBox = new MessageBox(gui.getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
		messageBox.setText(Resources.get("msg.confirm.close.text"));
		messageBox.setMessage(Resources.get("msg.confirm.close.message"));
		return messageBox.open() == SWT.YES;
	}

}

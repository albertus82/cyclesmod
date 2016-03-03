package it.albertus.cycles.gui;

import it.albertus.cycles.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;

public class CloseButtonListener implements Listener {

	private final Gui gui;

	public CloseButtonListener(Gui gui) {
		this.gui = gui;
	}

	@Override
	public void handleEvent(Event event) {
		final MessageBox messageBox = new MessageBox(gui.getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
		messageBox.setText(Resources.get("msg.confirm.close.text"));
		messageBox.setMessage(Resources.get("msg.confirm.close.message"));
		event.doit = messageBox.open() == SWT.YES;
	}

}

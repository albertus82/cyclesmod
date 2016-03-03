package it.albertus.cycles.gui;

import it.albertus.cycles.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MessageBox;

public class ExitSelectionListener extends SelectionAdapter {

	private final Gui gui;

	public ExitSelectionListener(Gui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		final MessageBox messageBox = new MessageBox(gui.getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
		messageBox.setText(Resources.get("msg.confirm.close.text"));
		messageBox.setMessage(Resources.get("msg.confirm.close.message"));
		if (messageBox.open() == SWT.YES) {
			gui.getShell().dispose();
		}
	}

}

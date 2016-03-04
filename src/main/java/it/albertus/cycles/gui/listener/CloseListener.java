package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.model.BikesCfg;
import it.albertus.cycles.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;

public class CloseListener extends SelectionAdapter implements Listener {

	private final CyclesModGui gui;

	public CloseListener(CyclesModGui gui) {
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
		gui.updateModelValues(true);
		if (!new BikesCfg(gui.getBikesInf()).getProperties().equals(gui.getLastPersistedProperties())) {
			final MessageBox messageBox = new MessageBox(gui.getShell(), SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
			messageBox.setText(Resources.get("msg.confirm.close.text"));
			messageBox.setMessage(Resources.get("msg.confirm.close.message"));
			switch (messageBox.open()) {
			case SWT.YES:
				return gui.save(false);
			case SWT.NO:
				return true;
			case SWT.CANCEL:
				return false;
			}
		}
		return true;
	}

}

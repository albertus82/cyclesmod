package it.albertus.cycles.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.model.BikesCfg;

public abstract class AskForSavingListener {

	protected final CyclesModGui gui;

	public AskForSavingListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	protected boolean askForSaving(final String dialogTitle, final String dialogMessage) {
		gui.updateModelValues(true);
		if (!new BikesCfg(gui.getBikesInf()).getMap().equals(gui.getLastPersistedProperties())) {
			final MessageBox messageBox = new MessageBox(gui.getShell(), SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
			messageBox.setText(dialogTitle);
			messageBox.setMessage(dialogMessage);
			final int selectedButton = messageBox.open();
			switch (selectedButton) {
			case SWT.YES:
				return gui.saveAs();
			case SWT.NO:
				return true;
			case SWT.CANCEL:
				return false;
			default:
				throw new IllegalStateException("Invalid button code: " + selectedButton);
			}
		}
		return true;
	}

}

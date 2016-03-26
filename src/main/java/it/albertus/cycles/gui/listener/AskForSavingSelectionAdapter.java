package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.model.BikesCfg;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.MessageBox;

public abstract class AskForSavingSelectionAdapter extends SelectionAdapter {

	protected final CyclesModGui gui;

	public AskForSavingSelectionAdapter(final CyclesModGui gui) {
		this.gui = gui;
	}

	protected boolean askForSaving(final String dialogTitle, final String dialogMessage) {
		gui.updateModelValues(true);
		if (!new BikesCfg(gui.getBikesInf()).getMap().equals(gui.getLastPersistedProperties())) {
			final MessageBox messageBox = new MessageBox(gui.getShell(), SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
			messageBox.setText(dialogTitle);
			messageBox.setMessage(dialogMessage);
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

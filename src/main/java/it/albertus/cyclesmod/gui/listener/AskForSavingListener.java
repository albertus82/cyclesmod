package it.albertus.cyclesmod.gui.listener;

import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import it.albertus.cyclesmod.common.engine.InvalidPropertyException;
import it.albertus.cyclesmod.common.model.BikesCfg;
import it.albertus.cyclesmod.gui.CyclesModGui;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public abstract class AskForSavingListener {

	@NonNull protected final CyclesModGui gui;

	protected boolean askForSaving(final String dialogTitle, final String dialogMessage) {
		try {
			gui.updateModelValues(true);
		}
		catch (final InvalidPropertyException e) {
			log.log(Level.FINE, "Invalid property \"" + e.getPropertyName() + "\":", e);
		}
		if (!new BikesCfg(gui.getEngine().getBikesInf()).getMap().equals(gui.getLastPersistedProperties())) {
			final MessageBox messageBox = new MessageBox(gui.getShell(), SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
			messageBox.setText(dialogTitle);
			messageBox.setMessage(dialogMessage);
			final int selectedButton = messageBox.open();
			switch (selectedButton) {
			case SWT.YES:
				return gui.save();
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

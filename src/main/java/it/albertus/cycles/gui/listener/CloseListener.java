package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.resources.Resources;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class CloseListener extends AskForSavingSelectionAdapter implements Listener {

	public CloseListener(final CyclesModGui gui) {
		super(gui);
	}

	/** Comando di chiusura da men&ugrave;. */
	@Override
	public void widgetSelected(SelectionEvent e) {
		if (askForSaving(Resources.get("msg.confirm.close.text"), Resources.get("msg.confirm.close.message"))) {
			gui.getShell().dispose();
		}
	}

	/** Pulsante di chiusura. */
	@Override
	public void handleEvent(Event event) {
		event.doit = askForSaving(Resources.get("msg.confirm.close.text"), Resources.get("msg.confirm.close.message"));
	}

}

package it.albertus.cycles.gui.listener;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.resources.Messages;

public class CloseListener extends AskForSavingListener implements ShellListener, SelectionListener, Listener {

	private static final String MSG_KEY_CONFIRM_CLOSE_MESSAGE = "msg.confirm.close.message";
	private static final String MSG_KEY_CONFIRM_CLOSE_TEXT = "msg.confirm.close.text";

	public CloseListener(final CyclesModGui gui) {
		super(gui);
	}

	/** Comando di chiusura da men&ugrave;. */
	@Override
	public void widgetSelected(final SelectionEvent event) {
		if (askForSaving(Messages.get(MSG_KEY_CONFIRM_CLOSE_TEXT), Messages.get(MSG_KEY_CONFIRM_CLOSE_MESSAGE))) {
			gui.getShell().dispose();
			event.display.dispose(); // Fix close not working on Windows 10 when iconified
		}
		else {
			event.doit = false;
		}
	}

	@Override
	public void handleEvent(final Event event) {
		if (askForSaving(Messages.get(MSG_KEY_CONFIRM_CLOSE_TEXT), Messages.get(MSG_KEY_CONFIRM_CLOSE_MESSAGE))) {
			gui.getShell().dispose();
			event.display.dispose(); // Fix close not working on Windows 10 when iconified
		}
		else {
			event.doit = false;
		}
	}

	@Override
	public void shellClosed(final ShellEvent event) {
		if (askForSaving(Messages.get(MSG_KEY_CONFIRM_CLOSE_TEXT), Messages.get(MSG_KEY_CONFIRM_CLOSE_MESSAGE))) {
			gui.getShell().dispose();
			event.display.dispose(); // Fix close not working on Windows 10 when iconified
		}
		else {
			event.doit = false;
		}
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {/* Ignore */}

	@Override
	public void shellActivated(final ShellEvent event) {/* Ignore */}

	@Override
	public void shellDeactivated(final ShellEvent event) {/* Ignore */}

	@Override
	public void shellDeiconified(final ShellEvent event) {/* Ignore */}

	@Override
	public void shellIconified(final ShellEvent event) {/* Ignore */}

}

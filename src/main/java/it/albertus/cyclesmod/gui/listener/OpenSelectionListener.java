package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.cyclesmod.gui.CyclesModGui;
import it.albertus.cyclesmod.gui.resources.GuiMessages;

public class OpenSelectionListener extends AskForSavingListener implements SelectionListener {

	private static final Messages messages = GuiMessages.INSTANCE;

	public OpenSelectionListener(final CyclesModGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		if (askForSaving(messages.get("gui.label.window.title"), messages.get("gui.message.confirm.open.message"))) {
			gui.open();
		}
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {/* Ignore */}

}

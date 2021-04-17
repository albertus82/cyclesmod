package it.albertus.cyclesmod.gui.listener;

import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MessageBox;

import it.albertus.cyclesmod.common.model.BikesInf;
import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.cyclesmod.gui.CyclesModGui;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import it.albertus.util.ExceptionUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class ResetAllSelectionListener extends SelectionAdapter {

	private static final Messages messages = GuiMessages.INSTANCE;

	@NonNull private final CyclesModGui gui;

	@Override
	public void widgetSelected(final SelectionEvent se) {
		MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		messageBox.setText(messages.get("gui.message.warning"));
		messageBox.setMessage(messages.get("gui.message.reset.overwrite.all"));
		int choose = messageBox.open();
		if (choose == SWT.YES) {
			try {
				reset();
			}
			catch (final Exception e) {
				log.log(Level.WARNING, e.toString(), e);
				messageBox = new MessageBox(gui.getShell(), SWT.ICON_ERROR);
				messageBox.setText(messages.get("gui.message.warning"));
				messageBox.setMessage(messages.get("gui.error.reset", ExceptionUtils.getUIMessage(e)));
				messageBox.open();
			}
		}
	}

	private void reset() {
		gui.setBikesInf(new BikesInf());
		gui.getTabs().updateFormValues();
	}

}

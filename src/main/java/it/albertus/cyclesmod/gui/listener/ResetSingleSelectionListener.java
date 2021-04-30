package it.albertus.cyclesmod.gui.listener;

import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MessageBox;

import it.albertus.cyclesmod.common.engine.InvalidPropertyException;
import it.albertus.cyclesmod.common.model.BikeType;
import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.cyclesmod.gui.CyclesModGui;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import it.albertus.util.ExceptionUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class ResetSingleSelectionListener extends SelectionAdapter {

	private static final Messages messages = GuiMessages.INSTANCE;

	@NonNull private final CyclesModGui gui;

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final BikeType type = BikeType.values()[gui.getTabs().getTabFolder().getSelectionIndex()];
		MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		messageBox.setText(messages.get("gui.message.warning"));
		messageBox.setMessage(messages.get("gui.message.reset.overwrite.single", type.getDisplacement()));
		int choose = messageBox.open();
		if (choose == SWT.YES) {
			try {
				reset(type);
			}
			catch (final Exception e) {
				log.log(Level.WARNING, "Cannot reset bike " + type, e);
				messageBox = new MessageBox(gui.getShell(), SWT.ICON_ERROR);
				messageBox.setText(messages.get("gui.message.warning"));
				messageBox.setMessage(messages.get("gui.error.reset", ExceptionUtils.getUIMessage(e)));
				messageBox.open();
			}
		}
	}

	private void reset(final BikeType type) {
		try {
			gui.updateModelValues(true);
		}
		catch (final InvalidPropertyException e) {
			log.log(Level.WARNING, "Invalid property \"" + e.getPropertyName() + "\":", e);
		}
		gui.getBikesInf().reset(type);
		gui.getTabs().updateFormValues();
	}

}

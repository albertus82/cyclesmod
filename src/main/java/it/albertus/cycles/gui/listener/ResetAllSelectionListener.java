package it.albertus.cycles.gui.listener;

import it.albertus.cycles.data.DefaultBikes;
import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.resources.Messages;
import it.albertus.util.ExceptionUtils;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MessageBox;

public class ResetAllSelectionListener extends SelectionAdapter {

	private final CyclesModGui gui;

	public ResetAllSelectionListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		messageBox.setText(Messages.get("msg.warning"));
		messageBox.setMessage(Messages.get("msg.reset.overwrite.all"));
		int choose = messageBox.open();
		if (choose == SWT.YES) {
			try {
				reset();
			}
			catch (Exception e) {
				System.err.println(ExceptionUtils.getLogMessage(e));
				messageBox = new MessageBox(gui.getShell(), SWT.ICON_ERROR);
				messageBox.setText(Messages.get("msg.warning"));
				messageBox.setMessage(Messages.get("err.reset", ExceptionUtils.getUIMessage(e)));
				messageBox.open();
			}
		}
	}

	private void reset() throws IOException {
		gui.setBikesInf(new BikesInf(new DefaultBikes().getInputStream()));
		gui.getTabs().updateFormValues();
	}

}

package it.albertus.cycles.gui.listener;

import it.albertus.cycles.data.DefaultBikes;
import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.resources.Resources;
import it.albertus.util.ExceptionUtils;

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
	public void widgetSelected(SelectionEvent event) {
		MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		messageBox.setText(Resources.get("msg.warning"));
		messageBox.setMessage(Resources.get("msg.reset.overwrite.all"));
		int choose = messageBox.open();
		if (choose == SWT.YES) {
			try {
				gui.setBikesInf(new BikesInf(new DefaultBikes().getInputStream()));
				gui.updateFormValues();
			}
			catch (Exception e) {
				System.err.println(ExceptionUtils.getLogMessage(e));
				messageBox = new MessageBox(gui.getShell(), SWT.ICON_ERROR);
				messageBox.setText(Resources.get("msg.warning"));
				messageBox.setMessage(Resources.get("err.reset", ExceptionUtils.getUIMessage(e)));
				messageBox.open();
			}
		}
	}

}

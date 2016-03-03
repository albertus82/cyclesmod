package it.albertus.cycles.gui;

import it.albertus.cycles.model.Bike;
import it.albertus.cycles.resources.Resources;
import it.albertus.util.ExceptionUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MessageBox;

public class ResetSelectionListener extends SelectionAdapter {

	private final CyclesModGui gui;

	public ResetSelectionListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		int choose = SWT.YES;
		if (gui.getBikesInf() != null) {
			MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			messageBox.setText(Resources.get("msg.warning"));
			messageBox.setMessage(Resources.get("msg.reset.overwrite"));
			choose = messageBox.open();
		}
		if (choose == SWT.YES) {
			try {
				gui.updateModelValues(true);
				gui.getBikesInf().reset(Bike.Type.values()[gui.getTabFolder().getSelectionIndex()]);
				gui.updateFormValues();
			}
			catch (Exception e) {
				System.err.println(ExceptionUtils.getLogMessage(e));
				MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_ERROR);
				messageBox.setText(Resources.get("msg.warning"));
				messageBox.setMessage(Resources.get("err.reset", ExceptionUtils.getUIMessage(e)));
				messageBox.open();
			}
		}
	}

}

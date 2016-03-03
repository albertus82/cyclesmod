package it.albertus.cycles.gui;

import it.albertus.cycles.engine.InvalidPropertyException;
import it.albertus.cycles.model.BikesInf;
import it.albertus.cycles.resources.Resources;
import it.albertus.util.ExceptionUtils;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;

public class SaveSelectionListener extends SelectionAdapter {

	private final CyclesModGui gui;

	public SaveSelectionListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		try {
			gui.updateModelValues(false);
		}
		catch (InvalidPropertyException ipe) {
			System.err.println(ExceptionUtils.getLogMessage(ipe));
			MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_ERROR);
			messageBox.setText(Resources.get("msg.warning"));
			messageBox.setMessage(ExceptionUtils.getUIMessage(ipe));
			messageBox.open();
			return;
		}
		FileDialog saveDialog = new FileDialog(gui.getShell(), SWT.SAVE);
		saveDialog.setFilterExtensions(new String[] { "*.INF; *.inf" });
		saveDialog.setFileName(BikesInf.FILE_NAME);
		saveDialog.setOverwrite(true);
		String fileName = saveDialog.open();

		if (StringUtils.isNotBlank(fileName)) {
			try {
				gui.getBikesInf().write(fileName);
			}
			catch (Exception e) {
				System.err.println(ExceptionUtils.getLogMessage(e));
				MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_ERROR);
				messageBox.setText(Resources.get("msg.warning"));
				messageBox.setMessage(Resources.get("err.file.save", ExceptionUtils.getUIMessage(e)));
				messageBox.open();
				return;
			}
			MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_INFORMATION);
			messageBox.setText(Resources.get("msg.completed"));
			messageBox.setMessage(Resources.get("msg.file.saved", fileName));
			messageBox.open();
		}
	}

}

package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.resources.Resources;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;

public class OpenSelectionListener extends AskForSavingSelectionAdapter {

	public OpenSelectionListener(CyclesModGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		if (askForSaving(Resources.get("win.title"), Resources.get("msg.confirm.open.message"))) {
			final FileDialog openDialog = new FileDialog(gui.getShell(), SWT.OPEN);
			openDialog.setFilterExtensions(new String[] { "*.INF; *.inf; *.CFG; *.cfg" });
			final String fileName = openDialog.open();
			if (StringUtils.isNotBlank(fileName)) {
				gui.load(fileName, true);
			}
		}
	}

}

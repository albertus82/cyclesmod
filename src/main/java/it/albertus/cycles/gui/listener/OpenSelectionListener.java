package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;
import it.albertus.cycles.resources.Messages;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;

public class OpenSelectionListener extends AskForSavingSelectionAdapter {

	private static final String[] EXTENSIONS = { "*.INF;*.inf;*.CFG;*.cfg" };

	public OpenSelectionListener(CyclesModGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		if (askForSaving(Messages.get("win.title"), Messages.get("msg.confirm.open.message"))) {
			final FileDialog openDialog = new FileDialog(gui.getShell(), SWT.OPEN);
			openDialog.setFilterExtensions(EXTENSIONS);
			final String fileName = openDialog.open();
			if (StringUtils.isNotBlank(fileName)) {
				gui.load(fileName, false);
			}
		}
	}

}

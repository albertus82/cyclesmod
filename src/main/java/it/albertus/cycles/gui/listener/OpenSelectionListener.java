package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.CyclesModGui;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;

public class OpenSelectionListener extends SelectionAdapter {

	private final CyclesModGui gui;

	public OpenSelectionListener(CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		FileDialog openDialog = new FileDialog(gui.getShell(), SWT.OPEN);
		openDialog.setFilterExtensions(new String[] { "*.INF; *.inf; *.CFG; *.cfg" });
		String fileName = openDialog.open();
		if (StringUtils.isNotBlank(fileName)) {
			gui.load(fileName, true);
		}
	}

}

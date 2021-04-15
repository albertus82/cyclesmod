package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.FileDialog;

import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.cyclesmod.gui.CyclesModGui;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import it.albertus.util.StringUtils;

public class OpenSelectionListener extends AskForSavingListener implements SelectionListener {

	private static final String[] EXTENSIONS = { "*.INF;*.inf;*.CFG;*.cfg" };

	private static final Messages messages = GuiMessages.INSTANCE;

	public OpenSelectionListener(final CyclesModGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		if (askForSaving(messages.get("win.title"), messages.get("msg.confirm.open.message"))) {
			final FileDialog openDialog = new FileDialog(gui.getShell(), SWT.OPEN);
			openDialog.setFilterExtensions(EXTENSIONS);
			final String fileName = openDialog.open();
			if (StringUtils.isNotBlank(fileName)) {
				gui.open(fileName);
			}
		}
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {/* Ignore */}

}

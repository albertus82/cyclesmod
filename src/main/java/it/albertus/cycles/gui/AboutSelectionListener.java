package it.albertus.cycles.gui;

import it.albertus.cycles.resources.Resources;
import it.albertus.util.NewLine;
import it.albertus.util.Version;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MessageBox;

public class AboutSelectionListener extends SelectionAdapter {

	private final Gui gui;

	public AboutSelectionListener(Gui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_INFORMATION);
		messageBox.setText(Resources.get("msg.info.title"));
		messageBox.setMessage(Resources.get("msg.info.body", Version.getInstance().getNumber(), Version.getInstance().getDate()) + NewLine.SYSTEM_LINE_SEPARATOR + Resources.get("msg.info.site") + NewLine.SYSTEM_LINE_SEPARATOR + Resources.get("msg.info.icon"));
		messageBox.open();
	}

}

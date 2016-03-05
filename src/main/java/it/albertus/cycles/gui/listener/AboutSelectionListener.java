package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.AboutDialog;
import it.albertus.cycles.gui.Gui;
import it.albertus.cycles.resources.Resources;
import it.albertus.util.Version;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class AboutSelectionListener extends SelectionAdapter {

	private final Gui gui;

	public AboutSelectionListener(Gui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		final AboutDialog aboutDialog = new AboutDialog(gui.getShell());
		aboutDialog.setText(Resources.get("msg.info.title"));
		aboutDialog.setMessage(Resources.get("msg.info.body", Version.getInstance().getNumber(), Version.getInstance().getDate()));
		aboutDialog.setApplicationUrl(Resources.get("msg.info.site"));
		aboutDialog.setIconUrl(Resources.get("msg.info.icon.site"));
		aboutDialog.open();
	}

}

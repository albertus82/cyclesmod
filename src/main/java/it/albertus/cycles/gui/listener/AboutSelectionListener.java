package it.albertus.cycles.gui.listener;

import it.albertus.cycles.gui.AboutDialog;
import it.albertus.cycles.resources.Messages;
import it.albertus.util.Version;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class AboutSelectionListener extends SelectionAdapter {

	private final IShellProvider gui;

	public AboutSelectionListener(IShellProvider gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		final AboutDialog aboutDialog = new AboutDialog(gui.getShell());
		aboutDialog.setText(Messages.get("msg.info.title"));
		aboutDialog.setMessage(Messages.get("msg.info.body", Version.getInstance().getNumber(), Version.getInstance().getDate()));
		aboutDialog.setApplicationUrl(Messages.get("msg.info.site"));
		aboutDialog.setIconUrl(Messages.get("msg.info.icon.site"));
		aboutDialog.open();
	}

}

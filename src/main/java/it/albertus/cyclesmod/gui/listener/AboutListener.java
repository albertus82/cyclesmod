package it.albertus.cyclesmod.gui.listener;

import java.text.DateFormat;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import it.albertus.cyclesmod.gui.AboutDialog;
import it.albertus.cyclesmod.resources.Messages;
import it.albertus.util.Version;

public class AboutListener extends SelectionAdapter implements Listener {

	private final IShellProvider gui;

	public AboutListener(final IShellProvider gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final AboutDialog aboutDialog = new AboutDialog(gui.getShell());
		aboutDialog.setText(Messages.get("msg.info.title"));
		final Version version = Version.getInstance();
		aboutDialog.setMessage(Messages.get("msg.info.body", version.getNumber(), DateFormat.getDateInstance(DateFormat.MEDIUM, Messages.getLanguage().getLocale()).format(version.getDate())));
		aboutDialog.setApplicationUrl(Messages.get("msg.info.site"));
		aboutDialog.setIconUrl(Messages.get("msg.info.icon.site"));
		aboutDialog.open();
	}

	@Override
	public void handleEvent(final Event event) {
		widgetSelected(null);
	}

}

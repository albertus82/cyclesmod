package it.albertus.cyclesmod.gui.listener;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import it.albertus.cyclesmod.gui.AboutDialog;
import it.albertus.cyclesmod.resources.Messages;
import it.albertus.util.Version;
import it.albertus.util.logging.LoggerFactory;

public class AboutListener extends SelectionAdapter implements Listener {

	private static final Logger logger = LoggerFactory.getLogger(AboutListener.class);

	private final IShellProvider gui;

	public AboutListener(final IShellProvider gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final AboutDialog aboutDialog = new AboutDialog(gui.getShell());
		aboutDialog.setText(Messages.get("msg.info.title"));
		Date versionDate;
		try {
			versionDate = Version.getDate();
		}
		catch (final Exception e) {
			logger.log(Level.WARNING, e.toString(), e);
			versionDate = new Date();
		}
		aboutDialog.setMessage(Messages.get("msg.info.body", Version.getNumber(), DateFormat.getDateInstance(DateFormat.MEDIUM, Messages.getLanguage().getLocale()).format(versionDate)));
		aboutDialog.setApplicationUrl(Messages.get("msg.info.site"));
		aboutDialog.setIconUrl(Messages.get("msg.info.icon.site"));
		aboutDialog.open();
	}

	@Override
	public void handleEvent(final Event event) {
		widgetSelected(null);
	}

}

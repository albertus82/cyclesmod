package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cyclesmod.common.resources.Messages;
import it.albertus.cyclesmod.common.resources.Messages.Language;
import it.albertus.cyclesmod.gui.CyclesModGui;
import it.albertus.jface.JFaceMessages;

public class LanguageSelectionListener extends SelectionAdapter {

	private final CyclesModGui gui;

	public LanguageSelectionListener(final CyclesModGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent e) {
		final MenuItem languageMenuItem = (MenuItem) e.widget;
		if (languageMenuItem.getSelection() && !Messages.getLanguage().equals(languageMenuItem.getData())) {
			final Language language = (Language) languageMenuItem.getData();
			gui.setLanguage(language);
			JFaceMessages.setLanguage(language.getLocale().getLanguage());
		}
	}

}

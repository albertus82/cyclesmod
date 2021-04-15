package it.albertus.cyclesmod.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.cyclesmod.common.resources.ConfigurableMessages;
import it.albertus.cyclesmod.common.resources.Language;
import it.albertus.cyclesmod.gui.CyclesModGui;
import it.albertus.cyclesmod.gui.resources.GuiMessages;
import it.albertus.jface.JFaceMessages;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LanguageSelectionListener extends SelectionAdapter {

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	@NonNull private final CyclesModGui gui;

	@Override
	public void widgetSelected(@NonNull final SelectionEvent e) {
		if (e.widget instanceof MenuItem) {
			final MenuItem languageMenuItem = (MenuItem) e.widget;
			if (languageMenuItem.getSelection() && !messages.getLanguage().equals(languageMenuItem.getData())) {
				final Language language = (Language) languageMenuItem.getData();
				gui.setLanguage(language);
				JFaceMessages.setLanguage(language.getLocale().getLanguage());
			}
		}
	}

}

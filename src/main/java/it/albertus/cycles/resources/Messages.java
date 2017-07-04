package it.albertus.cycles.resources;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.util.logging.LoggerFactory;

public final class Messages {

	private static final Logger logger = LoggerFactory.getLogger(Messages.class);

	public enum Language {
		ENGLISH(Locale.ENGLISH),
		ITALIAN(Locale.ITALIAN);

		private final Locale locale;

		private Language(final Locale locale) {
			this.locale = locale;
		}

		public Locale getLocale() {
			return locale;
		}
	}

	private static final String BASE_NAME = Messages.class.getName().toLowerCase();

	private static ResourceBundle resources = ResourceBundle.getBundle(BASE_NAME, ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));

	/** Instantiation not permitted. */
	private Messages() {
		throw new IllegalAccessError();
	}

	/** Aggiorna la lingua in cui vengono mostrati i messaggi. */
	public static void setLanguage(final Language language) {
		if (language != null) {
			resources = ResourceBundle.getBundle(BASE_NAME, language.getLocale(), ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
		}
	}

	public static Language getLanguage() {
		for (final Language language : Language.values()) {
			if (language.locale.equals(resources.getLocale())) {
				return language;
			}
		}
		return Language.ENGLISH; // Default.
	}

	public static String get(final String key, final Object... params) {
		final List<String> stringParams = new ArrayList<String>(params.length);
		for (final Object param : params) {
			stringParams.add(String.valueOf(param));
		}
		String message;
		try {
			message = MessageFormat.format(resources.getString(key), stringParams.toArray());
			message = message != null ? message.trim() : "";
		}
		catch (final MissingResourceException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			message = key;
		}
		return message;
	}

}

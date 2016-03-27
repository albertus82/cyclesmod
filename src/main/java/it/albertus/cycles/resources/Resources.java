package it.albertus.cycles.resources;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class Resources {

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

	private static final String BASE_NAME = Resources.class.getName().toLowerCase();

	private static ResourceBundle resources = ResourceBundle.getBundle(BASE_NAME, ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));

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
			stringParams.add(param != null ? param.toString() : "");
		}
		final String message = MessageFormat.format(resources.getString(key), stringParams.toArray());
		return message != null ? message.trim() : "";
	}

}

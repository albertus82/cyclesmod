package com.github.albertus82.cyclesmod.common.resources;

import java.util.Collection;
import java.util.Locale;

import it.albertus.jface.JFaceMessages;
import lombok.NonNull;

public enum CommonMessages implements ConfigurableMessages {

	INSTANCE;

	private static final ConfigurableMessages fallbackMessages = FallbackMessages.INSTANCE;

	private final MessageBundle bundle = new MessageBundle(getClass().getName().toLowerCase(Locale.ROOT));

	@Override
	public String get(@NonNull final String key) {
		return bundle.getMessage(key, fallbackMessages::get);
	}

	@Override
	public String get(@NonNull final String key, @NonNull final Object... params) {
		return bundle.getMessage(key, params, fallbackMessages::get);
	}

	@Override
	public Language getLanguage() {
		return bundle.getLanguage();
	}

	@Override
	public void setLanguage(@NonNull final Language language) { // NOSONAR Enum singleton
		bundle.setLanguage(language, fallbackMessages::setLanguage);
	}

	@Override
	public Collection<String> getKeys() {
		return bundle.getKeys(fallbackMessages::getKeys);
	}

	private enum FallbackMessages implements ConfigurableMessages {

		INSTANCE;

		@Override
		public String get(@NonNull final String key) {
			return JFaceMessages.get(key);
		}

		@Override
		public String get(@NonNull final String key, @NonNull final Object... params) {
			return JFaceMessages.get(key, params);
		}

		@Override
		public Language getLanguage() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setLanguage(@NonNull final Language language) { // NOSONAR Enum singleton
			JFaceMessages.setLanguage(language.getLocale().getLanguage());
		}

		@Override
		public Collection<String> getKeys() {
			return JFaceMessages.getKeys();
		}
	}

}

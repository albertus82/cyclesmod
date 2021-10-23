package com.github.albertus82.cyclesmod.gui.resources;

import java.util.Collection;
import java.util.Locale;

import com.github.albertus82.cyclesmod.common.resources.CommonMessages;
import com.github.albertus82.cyclesmod.common.resources.ConfigurableMessages;
import com.github.albertus82.cyclesmod.common.resources.Language;
import com.github.albertus82.cyclesmod.common.resources.MessageBundle;

import lombok.NonNull;

public enum GuiMessages implements ConfigurableMessages {

	INSTANCE;

	private static final ConfigurableMessages fallbackMessages = CommonMessages.INSTANCE;

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

}

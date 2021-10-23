package com.github.albertus82.cyclesmod.common.resources;

import lombok.NonNull;

public interface ConfigurableMessages extends Messages {

	Language getLanguage();

	void setLanguage(@NonNull final Language language);

}

package io.github.albertus82.cyclesmod.common.resources;

import java.util.Collection;

import lombok.NonNull;

public interface Messages {

	String get(@NonNull String key);

	String get(@NonNull String key, @NonNull Object... params);

	Collection<String> getKeys();

}

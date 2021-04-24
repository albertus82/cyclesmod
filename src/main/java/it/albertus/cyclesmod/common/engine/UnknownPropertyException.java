package it.albertus.cyclesmod.common.engine;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class UnknownPropertyException extends InvalidPropertyException {

	private static final long serialVersionUID = 8358143057948555987L;

	public UnknownPropertyException(@NonNull final String propertyName) {
		super(propertyName, "Unknown property name: " + propertyName);
	}

}

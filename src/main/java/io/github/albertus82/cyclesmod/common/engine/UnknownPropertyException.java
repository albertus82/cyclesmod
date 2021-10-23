package io.github.albertus82.cyclesmod.common.engine;

import lombok.Getter;

@Getter
public class UnknownPropertyException extends InvalidPropertyException {

	private static final long serialVersionUID = 8358143057948555987L;

	public UnknownPropertyException(final String propertyName) {
		super(propertyName, "Unknown property name: " + propertyName);
	}

}

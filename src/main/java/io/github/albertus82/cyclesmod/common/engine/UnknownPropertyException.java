package io.github.albertus82.cyclesmod.common.engine;

import lombok.Getter;

@Getter
public class UnknownPropertyException extends InvalidPropertyException {

	private static final long serialVersionUID = 554994126110006337L;

	public UnknownPropertyException(final String propertyName) {
		super(propertyName, "Unknown property name: " + propertyName);
	}

	public UnknownPropertyException(final String propertyName, final Throwable cause) {
		super(propertyName, "Unknown property name: " + propertyName, cause);
	}

}

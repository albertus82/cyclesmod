package com.github.albertus82.cyclesmod.common.engine;

import lombok.Getter;

@Getter
public class InvalidNumberException extends InvalidPropertyException {

	private static final long serialVersionUID = 3831930136276145792L;

	private final String value;
	private final int radix;

	public InvalidNumberException(final String propertyName, final String value, final int radix) {
		super(propertyName, "For input string: \"" + value + "\" (radix: " + radix + ")");
		this.value = value;
		this.radix = radix;
	}

	public InvalidNumberException(final String propertyName, final String value, final int radix, final Throwable cause) {
		super(propertyName, "For input string: \"" + value + "\" (radix: " + radix + ")", cause);
		this.value = value;
		this.radix = radix;
	}

}

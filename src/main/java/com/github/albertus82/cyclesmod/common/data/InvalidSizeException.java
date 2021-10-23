package com.github.albertus82.cyclesmod.common.data;

import lombok.Getter;

@Getter
public class InvalidSizeException extends Exception {

	private static final long serialVersionUID = -3304205069550661343L;

	private final long expected;
	private final long actual;

	public InvalidSizeException(final long expected, final long actual) {
		super("Expected: " + expected + " but was: " + actual);
		this.expected = expected;
		this.actual = actual;
	}

}

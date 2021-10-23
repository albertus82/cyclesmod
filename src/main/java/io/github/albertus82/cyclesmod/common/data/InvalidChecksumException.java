package io.github.albertus82.cyclesmod.common.data;

import lombok.Getter;

@Getter
public class InvalidChecksumException extends Exception {

	private static final long serialVersionUID = 4094874333807513801L;

	private final long expected;
	private final long actual;

	public InvalidChecksumException(final long expected, final long actual) {
		super(String.format("Expected: 0x%08X but was: 0x%08X", expected, actual));
		this.expected = expected;
		this.actual = actual;
	}

}

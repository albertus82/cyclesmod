package it.albertus.cyclesmod.common.engine;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class InvalidNumberException extends Exception {

	private static final long serialVersionUID = 3831930136276145792L;

	private final String value;
	private final int radix;

	public InvalidNumberException(final String value, final int radix) {
		super();
		this.value = value;
		this.radix = radix;
	}

	public InvalidNumberException(final String value, final int radix, @NonNull final Throwable cause) {
		super(cause);
		this.value = value;
		this.radix = radix;
	}

}

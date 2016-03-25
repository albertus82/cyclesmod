package it.albertus.cycles.engine;

public enum NumeralSystem {
//	OCTAL(8),
	DECIMAL(10),
	HEXADECIMAL(16);

	public static final NumeralSystem DEFAULT = DECIMAL;

	private final int radix;

	private NumeralSystem(final int radix) {
		this.radix = radix;
	}

	public int getRadix() {
		return radix;
	}

}

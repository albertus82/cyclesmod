package io.github.albertus82.cyclesmod.common.engine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NumeralSystem {

	DECIMAL(10),
	HEXADECIMAL(16);

	public static final NumeralSystem DEFAULT = DECIMAL;

	private final int radix;

}

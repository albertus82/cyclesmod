package com.github.albertus82.cyclesmod.common.engine;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class ValueOutOfRangeException extends InvalidPropertyException {

	private static final long serialVersionUID = 6588117863627010925L;

	private final Number value;
	private final Number minValue;
	private final Number maxValue;

	public ValueOutOfRangeException(@NonNull final String propertyName, @NonNull final Number value, @NonNull final Number minValue, @NonNull final Number maxValue) {
		super(propertyName, "Value out of range: " + value + " (must be between " + minValue + " and " + maxValue + ")");
		this.value = value;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

}

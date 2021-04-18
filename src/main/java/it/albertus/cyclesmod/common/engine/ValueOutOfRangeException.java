package it.albertus.cyclesmod.common.engine;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class ValueOutOfRangeException extends Exception {

	private static final long serialVersionUID = 6588117863627010925L;

	private final Number value;

	private final Number minValue;
	private final Number maxValue;

	public ValueOutOfRangeException(@NonNull final Number value, @NonNull final Number minValue, @NonNull final Number maxValue) {
		super("Value out of range: " + value + " (valid range between " + minValue + " and " + maxValue + ")");
		this.value = value;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

}

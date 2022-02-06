package io.github.albertus82.cyclesmod.common.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.Map;

import io.github.albertus82.cyclesmod.common.engine.InvalidNumberException;
import io.github.albertus82.cyclesmod.common.engine.ValueOutOfRangeException;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Settings implements ByteArray {

	public static final int LENGTH = 22;
	public static final int MIN_VALUE = 0;
	public static final int MAX_VALUE = 0xFFFF;

	public static final String PREFIX = "settings";

	private final Map<Setting, Integer> values = new EnumMap<>(Setting.class);

	public Settings(final int gearsCount, final int rpmRedline, final int rpmLimit, final int overrevTolerance, final int grip, final int grip0, final int brakingSpeed, final int brakingSpeed0, final int spinThreshold, final int spinThreshold0, final int rpmDownshift) {
		values.put(Setting.GEARS_COUNT, gearsCount);
		values.put(Setting.RPM_REDLINE, rpmRedline);
		values.put(Setting.RPM_LIMIT, rpmLimit);
		values.put(Setting.OVERREV_TOLERANCE, overrevTolerance);
		values.put(Setting.GRIP, grip);
		values.put(Setting.GRIP_0, grip0);
		values.put(Setting.BRAKING_SPEED, brakingSpeed);
		values.put(Setting.BRAKING_SPEED_0, brakingSpeed0);
		values.put(Setting.SPIN_THRESHOLD, spinThreshold);
		values.put(Setting.SPIN_THRESHOLD_0, spinThreshold0);
		values.put(Setting.RPM_DOWNSHIFT, rpmDownshift);
	}

	@Override
	public byte[] toByteArray() {
		final ByteBuffer buf = ByteBuffer.allocate(LENGTH).order(ByteOrder.LITTLE_ENDIAN);
		for (final int value : values.values()) {
			buf.putShort((short) value);
		}
		return buf.array();
	}

	public static int parse(@NonNull final String propertyName, final String value, final int radix) throws ValueOutOfRangeException, InvalidNumberException {
		if (value == null) {
			throw new InvalidNumberException(propertyName, value, radix, new NullPointerException("value is null"));
		}
		if (value.trim().isEmpty()) {
			throw new InvalidNumberException(propertyName, value, radix);
		}
		final long newValue;
		try {
			newValue = Long.parseLong(value.trim(), radix);
		}
		catch (final NumberFormatException e) {
			throw new InvalidNumberException(propertyName, value, radix, e);
		}
		if (newValue < MIN_VALUE || newValue > MAX_VALUE) {
			throw new ValueOutOfRangeException(propertyName, newValue, MIN_VALUE, MAX_VALUE);
		}
		return (int) newValue;
	}

}

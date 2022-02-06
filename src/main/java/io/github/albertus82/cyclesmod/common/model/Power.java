package io.github.albertus82.cyclesmod.common.model;

import java.nio.ByteBuffer;
import java.util.Arrays;

import io.github.albertus82.cyclesmod.common.engine.InvalidNumberException;
import io.github.albertus82.cyclesmod.common.engine.ValueOutOfRangeException;
import io.github.albertus82.cyclesmod.common.resources.CommonMessages;
import io.github.albertus82.cyclesmod.common.resources.Messages;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Power implements ByteArray {

	public static final int LENGTH = 106;
	public static final short MIN_VALUE = 0;
	public static final short MAX_VALUE = 0xff;

	public static final String PREFIX = "power";

	private static final Messages messages = CommonMessages.INSTANCE;

	/**
	 * 0-767: overlap with Gearbox '7' (0-255), '8' (256-511) e '9' (512-767).
	 * However, the range 0-767 RPM is below the idle speed and therefore useless.
	 */
	public static final short BASE_RPM = 768;
	public static final short POINT_WIDTH_RPM = 128;

	/**
	 * 42-147: power curve (RPM range: 768-14335 RPM).
	 */
	private final short[] curve;

	public Power(@NonNull final short[] curve) {
		if (curve.length > LENGTH) {
			throw new IllegalArgumentException(messages.get("common.error.power", LENGTH, curve.length));
		}
		this.curve = Arrays.copyOf(curve, LENGTH);
	}

	@Override
	public byte[] toByteArray() {
		final ByteBuffer buf = ByteBuffer.allocate(LENGTH);
		for (final short point : curve) {
			buf.put((byte) point);
		}
		return buf.array();
	}

	public static int getRpm(final int index) {
		return BASE_RPM + POINT_WIDTH_RPM * index;
	}

	public static int indexOf(final double rpm) {
		return (int) (rpm + (double) POINT_WIDTH_RPM / 2 - BASE_RPM) / POINT_WIDTH_RPM;
	}

	public static short parse(@NonNull final String propertyName, final String value, final int radix) throws InvalidNumberException, ValueOutOfRangeException {
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
		return (short) newValue;
	}

}

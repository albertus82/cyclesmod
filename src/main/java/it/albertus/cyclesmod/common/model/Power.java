package it.albertus.cyclesmod.common.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import it.albertus.cyclesmod.common.engine.InvalidPropertyException;
import it.albertus.cyclesmod.common.resources.CommonMessages;
import it.albertus.cyclesmod.common.resources.Messages;
import lombok.NonNull;

public class Power implements ByteList {

	public static final int LENGTH = 106;
	public static final short MIN_VALUE = 0;
	public static final short MAX_VALUE = 255;

	public static final String PREFIX = "power";

	private static final Messages messages = CommonMessages.INSTANCE;

	/**
	 * 0-767: overlap con Gearbox '7' (0-255), '8' (256-511) e '9' (512-767). Il
	 * range 0-767 RPM e' comunque inferiore al regime minimo, quindi di fatto
	 * inutile.
	 */
	public static final short BASE_RPM = 768;
	public static final short POINT_WIDTH_RPM = 128;

	/**
	 * 42-147: curva di potenza (intervallo regime considerato: 768-14335 RPM).
	 */
	private final short[] curve;

	public Power(@NonNull final short[] curve) {
		if (curve.length > LENGTH) {
			throw new IllegalArgumentException(messages.get("common.err.power", LENGTH, curve.length));
		}
		this.curve = Arrays.copyOf(curve, LENGTH);
	}

	@Override
	public List<Byte> toByteList() {
		final List<Byte> byteList = new ArrayList<>(LENGTH);
		for (final short point : curve) {
			byteList.add((byte) point);
		}
		return byteList;
	}

	public static int getRpm(final int index) {
		return BASE_RPM + POINT_WIDTH_RPM * index;
	}

	public static int indexOf(final double rpm) {
		return (int) (rpm + (double) POINT_WIDTH_RPM / 2 - BASE_RPM) / POINT_WIDTH_RPM;
	}

	public static short parse(final String key, @NonNull final String value, final int radix) {
		final long newValue = Long.parseLong(value.trim(), radix);
		if (newValue < MIN_VALUE || newValue > MAX_VALUE) {
			throw new InvalidPropertyException(messages.get("common.err.illegal.value", Integer.toString(MIN_VALUE, radix).toUpperCase(Locale.ROOT), Integer.toString(MAX_VALUE, radix).toUpperCase(Locale.ROOT), key, Long.toString(newValue, radix).toUpperCase(Locale.ROOT)));
		}
		return (short) newValue;
	}

	public short[] getCurve() {
		return curve;
	}

}

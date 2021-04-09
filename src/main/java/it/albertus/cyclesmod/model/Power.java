package it.albertus.cyclesmod.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.albertus.cyclesmod.engine.InvalidPropertyException;
import it.albertus.cyclesmod.resources.Messages;
import lombok.NonNull;

public class Power implements ByteList {

	public static final int LENGTH = 106;
	public static final short MIN_VALUE = 0;
	public static final short MAX_VALUE = 255;

	public static final String PREFIX = "power";

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
			throw new IllegalArgumentException(Messages.get("err.power", LENGTH, curve.length));
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
			throw new InvalidPropertyException(Messages.get("err.illegal.value", Integer.toString(MIN_VALUE, radix).toUpperCase(), Integer.toString(MAX_VALUE, radix).toUpperCase(), key, Long.toString(newValue, radix).toUpperCase()));
		}
		return (short) newValue;
	}

	public short[] getCurve() {
		return curve;
	}

}

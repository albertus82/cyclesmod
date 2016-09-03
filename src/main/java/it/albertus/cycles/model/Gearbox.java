package it.albertus.cycles.model;

import it.albertus.cycles.engine.InvalidPropertyException;
import it.albertus.cycles.resources.Messages;
import it.albertus.util.ByteUtils;

import java.util.ArrayList;
import java.util.List;

public class Gearbox extends BikesInfElement {

	public static final int LENGTH = 20;
	public static final int MIN_VALUE = 0;

	/**
	 * I valori sono a 16 bit, ma di fatto vengono considerati solo 8 bit (si
	 * procede per multipli di 256).
	 */
	public static final int MAX_VALUE = 65535;

	/** 22-41 (N, 1, 2, 3, 4, 5, 6, 7, 8, 9) */
	private final int[] ratios = new int[LENGTH / 2];

	public Gearbox(final int[] ratios) {
		if (ratios.length > LENGTH / 2) {
			throw new IllegalArgumentException(Messages.get("err.gearbox", LENGTH / 2, ratios.length));
		}
		for (int i = 0; i < ratios.length; i++) {
			this.ratios[i] = ratios[i];
		}
	}

	@Override
	public List<Byte> toByteList() {
		final List<Byte> byteList = new ArrayList<Byte>(LENGTH);
		for (final int ratio : ratios) {
			byteList.addAll(ByteUtils.toByteList(ratio));
		}
		return byteList;
	}

	public static int parse(final String key, final String value, final int radix) {
		final long newValue = Long.parseLong(value.trim(), radix);
		if (newValue < MIN_VALUE || newValue > MAX_VALUE) {
			throw new InvalidPropertyException(Messages.get("err.illegal.value", Integer.toString(MIN_VALUE, radix).toUpperCase(), Integer.toString(MAX_VALUE, radix).toUpperCase(), key, Long.toString(newValue, radix).toUpperCase()));
		}
		return (int) newValue;
	}

	public int[] getRatios() {
		return ratios;
	}

}

package io.github.albertus82.cyclesmod.common.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.github.albertus82.cyclesmod.common.engine.InvalidNumberException;
import io.github.albertus82.cyclesmod.common.engine.ValueOutOfRangeException;
import io.github.albertus82.cyclesmod.common.resources.CommonMessages;
import io.github.albertus82.cyclesmod.common.resources.Messages;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Gearbox implements ByteArray {

	public static final int LENGTH = 20;
	public static final int MIN_VALUE = 0;

	public static final String PREFIX = "gearbox";

	private static final Messages messages = CommonMessages.INSTANCE;

	/**
	 * I valori sono a 16 bit, ma di fatto vengono considerati solo 8 bit (si
	 * procede per multipli di 256).
	 */
	public static final int MAX_VALUE = 0xFFFF;

	@Getter
	private static final Map<Game, Set<Byte>> validGears;

	static {
		final Map<Game, Set<Byte>> map = new EnumMap<>(Game.class);
		map.put(Game.GPC, Collections.unmodifiableSet(IntStream.rangeClosed(1, 6).boxed().map(Integer::byteValue).collect(Collectors.toSet())));
		map.put(Game.CYCLES, Collections.unmodifiableSet(IntStream.rangeClosed(1, 9).boxed().map(Integer::byteValue).collect(Collectors.toSet())));
		validGears = Collections.unmodifiableMap(map);
	}

	/** 22-41 (N, 1, 2, 3, 4, 5, 6, 7, 8, 9) */
	private final int[] ratios;

	public Gearbox(@NonNull final int[] ratios) {
		if (ratios.length > LENGTH / 2) {
			throw new IllegalArgumentException(messages.get("common.error.gearbox", LENGTH / 2, ratios.length));
		}
		this.ratios = Arrays.copyOf(ratios, LENGTH / 2);
	}

	@Override
	public byte[] toByteArray() {
		final ByteBuffer buf = ByteBuffer.allocate(LENGTH).order(ByteOrder.LITTLE_ENDIAN);
		for (final int ratio : ratios) {
			buf.putShort((short) ratio);
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

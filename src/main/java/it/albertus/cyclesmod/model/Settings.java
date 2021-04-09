package it.albertus.cyclesmod.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import it.albertus.cyclesmod.engine.InvalidPropertyException;
import it.albertus.cyclesmod.resources.Messages;
import it.albertus.util.ByteUtils;

public class Settings implements BikesInfElement {

	public static final int LENGTH = 22;
	public static final int MIN_VALUE = 0;
	public static final int MAX_VALUE = 65535;

	private final Map<Setting, Integer> values = new EnumMap<>(Setting.class);

	public Settings(int gearsCount, int rpmRedline, int rpmLimit, int overspeedGracePeriod, int grip, int unknown1, int brakingSpeed, int unknown2, int spinThreshold, int unknown3, int rpmDownshift) {
		values.put(Setting.GEARS_COUNT, gearsCount);
		values.put(Setting.RPM_REDLINE, rpmRedline);
		values.put(Setting.RPM_LIMIT, rpmLimit);
		values.put(Setting.OVERSPEED_GRACE_PERIOD, overspeedGracePeriod);
		values.put(Setting.GRIP, grip);
		values.put(Setting.UNKNOWN_1, unknown1);
		values.put(Setting.BRAKING_SPEED, brakingSpeed);
		values.put(Setting.UNKNOWN_2, unknown2);
		values.put(Setting.SPIN_THRESHOLD, spinThreshold);
		values.put(Setting.UNKNOWN_3, unknown3);
		values.put(Setting.RPM_DOWNSHIFT, rpmDownshift);
	}

	@Override
	public List<Byte> toByteList() {
		final List<Byte> byteList = new ArrayList<>(LENGTH);
		for (final int value : values.values()) {
			byteList.addAll(ByteUtils.toByteList(value));
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

	public Map<Setting, Integer> getValues() {
		return values;
	}

}

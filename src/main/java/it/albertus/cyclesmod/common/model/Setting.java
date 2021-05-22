package it.albertus.cyclesmod.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Setting { // The order matters.

	GEARS_COUNT(0, "gearsCount", 1, 9, true),
	RPM_REDLINE(1, "rpmRedline", 8500, 32767, true),
	RPM_LIMIT(2, "rpmLimit", 768, 14335, true), // 26239
	OVERREV_TOLERANCE(3, "overrevTolerance", 0, 32767, true),
	GRIP(4, "grip", 0, 65535, true),
	GRIP_0(5, "grip0", 0, 65535, false),
	BRAKING_SPEED(6, "brakingSpeed", 0, 65535, true),
	BRAKING_SPEED_0(7, "brakingSpeed0", 0, 65535, false),
	SPIN_THRESHOLD(8, "spinThreshold", 0, 32767, true),
	SPIN_THRESHOLD_0(9, "spinThreshold0", 0, 32767, false),
	RPM_DOWNSHIFT(10, "rpmDownshift", 0, 32767, true);

	private final int index;
	private final String key;
	private final int minValue;
	private final int maxValue;
	private final boolean active;

	public static Setting forKey(final String name) {
		for (final Setting setting : Setting.values()) {
			if (setting.getKey().equalsIgnoreCase(name)) { // Case insensitive.
				return setting;
			}
		}
		return null;
	}

	public static Setting forIndex(final int index) {
		for (final Setting setting : Setting.values()) {
			if (setting.getIndex() == index) {
				return setting;
			}
		}
		return null;
	}

}

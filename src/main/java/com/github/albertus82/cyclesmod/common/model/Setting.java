package com.github.albertus82.cyclesmod.common.model;

import static com.github.albertus82.cyclesmod.common.model.Game.CYCLES;
import static com.github.albertus82.cyclesmod.common.model.Game.GPC;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;

@Getter
public enum Setting { // The order matters.

	GEARS_COUNT(0, "gearsCount", 1, 9, CYCLES, GPC),
	RPM_REDLINE(1, "rpmRedline", 8500, 32767, CYCLES, GPC),
	RPM_LIMIT(2, "rpmLimit", 768, 14335, CYCLES, GPC), // 26239
	OVERREV_TOLERANCE(3, "overrevTolerance", 0, 32767, CYCLES, GPC),
	GRIP(4, "grip", 0, 65535, CYCLES, GPC),
	GRIP_0(5, "grip0", 0, 65535, GPC), // GPC only (pit stop)
	BRAKING_SPEED(6, "brakingSpeed", 0, 65535, CYCLES, GPC),
	BRAKING_SPEED_0(7, "brakingSpeed0", 0, 65535, GPC), // GPC only (pit stop)
	SPIN_THRESHOLD(8, "spinThreshold", 0, 32767, CYCLES, GPC),
	SPIN_THRESHOLD_0(9, "spinThreshold0", 0, 32767, GPC), // GPC only (pit stop)
	RPM_DOWNSHIFT(10, "rpmDownshift", 0, 32767, CYCLES, GPC);

	private final int index;
	private final String key;
	private final int minValue;
	private final int maxValue;
	private final Set<Game> games = EnumSet.noneOf(Game.class);

	private Setting(final int index, @NonNull final String key, final int minValue, final int maxValue, @NonNull final Game... games) {
		this.index = index;
		this.key = key;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.games.addAll(Arrays.asList(games));
	}

	public static Setting forKey(final String name) {
		for (final Setting setting : Setting.values()) {
			if (setting.key.equalsIgnoreCase(name)) { // Case insensitive.
				return setting;
			}
		}
		return null;
	}

	public static Setting forIndex(final int index) {
		for (final Setting setting : Setting.values()) {
			if (setting.index == index) {
				return setting;
			}
		}
		return null;
	}

}

package com.github.albertus82.cyclesmod.gui;

import com.github.albertus82.cyclesmod.common.model.Game;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Mode {

	CYCLES(Game.CYCLES),
	GPC(Game.GPC);

	public static final Mode DEFAULT = CYCLES;

	@NonNull
	private final Game game;

	public static Mode forGame(@NonNull final Game game) {
		for (final Mode mode : values()) {
			if (mode.game.equals(game)) {
				return mode;
			}
		}
		return null;
	}

}

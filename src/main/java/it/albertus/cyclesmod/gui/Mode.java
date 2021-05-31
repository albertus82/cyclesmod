package it.albertus.cyclesmod.gui;

import it.albertus.cyclesmod.common.model.Game;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Mode {

	CYCLES(Game.CYCLES),
	GPC(Game.GPC);

	private final Game game;

	public static final Mode DEFAULT = CYCLES;

	public static Mode forGame(@NonNull final Game game) {
		switch (game) {
		case CYCLES:
			return CYCLES;
		case GPC:
			return GPC;
		default:
			throw new IllegalArgumentException("Unknown game: " + game);
		}
	}

}

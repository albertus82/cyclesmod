package io.github.albertus82.cyclesmod.common.model;

import java.util.Locale;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleType {

	FERRARI_125(0, "Ferrari", 125),
	MCLAREN_250(1, "McLaren", 250),
	WILLIAMS_500(2, "Williams", 500);

	private final int index;
	@NonNull
	private final String team;
	private final int displacement;

	public String getKey(@NonNull final Game game) {
		switch (game) {
		case CYCLES:
			return Integer.toString(displacement);
		case GPC:
			return team.toLowerCase(Locale.ROOT);
		default:
			throw new IllegalArgumentException("Unknown or unsupported game: " + game);
		}
	}

	/** @return the language-independent description of this vehicle type. */
	public String getDescription(@NonNull final Game game) {
		switch (game) {
		case CYCLES:
			return displacement + " cc";
		case GPC:
			return team;
		default:
			throw new IllegalArgumentException("Unknown or unsupported game: " + game);
		}
	}

	public static VehicleType forDisplacement(final int displacement) {
		for (final VehicleType vehicleType : values()) {
			if (vehicleType.displacement == displacement) {
				return vehicleType;
			}
		}
		throw new IllegalArgumentException("No enum constant " + VehicleType.class.getCanonicalName() + " for displacement " + displacement);
	}

	public static VehicleType forTeam(@NonNull final String team) {
		for (final VehicleType vehicleType : values()) {
			if (vehicleType.team.equalsIgnoreCase(team)) {
				return vehicleType;
			}
		}
		throw new IllegalArgumentException("No enum constant " + VehicleType.class.getCanonicalName() + " for team " + team);
	}

}

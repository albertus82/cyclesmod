package it.albertus.cyclesmod.common.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleType {

	FERRARI_125(1, "Ferrari", 125),
	MCLAREN_250(2, "McLaren", 250),
	WILLIAMS_500(3, "Williams", 500);

	private final int index;
	@NonNull private final String team;
	private final int displacement;

	/** @return the language-independent description of this vehicle type. */
	public String getDescription(@NonNull final Game game) {
		switch (game) {
		case CYCLES:
			return displacement + " cc";
		case GPC:
			return team;
		default:
			throw new IllegalArgumentException("Unknown game: " + game);
		}
	}

	public static VehicleType get(final int index) {
		for (final VehicleType vehicleType : values()) {
			if (vehicleType.index == index) {
				return vehicleType;
			}
		}
		return null;
	}

	public static VehicleType forDisplacement(final int displacement) {
		for (final VehicleType vehicleType : values()) {
			if (vehicleType.displacement == displacement) {
				return vehicleType;
			}
		}
		return null;
	}

	public static VehicleType forTeam(final String team) {
		for (final VehicleType vehicleType : values()) {
			if (vehicleType.team.equalsIgnoreCase(team)) {
				return vehicleType;
			}
		}
		return null;
	}

}

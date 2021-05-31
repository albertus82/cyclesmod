package it.albertus.cyclesmod.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleType {

	FERRARI_125("Ferrari", 125),
	MCLAREN_250("McLaren", 250),
	WILLIAMS_500("Williams", 500);

	private final String team;
	private final int displacement;

	public static VehicleType forDisplacement(final int displacement) {
		for (final VehicleType bikeType : values()) {
			if (bikeType.displacement == displacement) {
				return bikeType;
			}
		}
		return null;
	}

}

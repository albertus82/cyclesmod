package it.albertus.cyclesmod.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleType {

	FERRARI_125(1, "Ferrari", 125),
	MCLAREN_250(2, "McLaren", 250),
	WILLIAMS_500(3, "Williams", 500);

	private final int index;
	private final String team;
	private final int displacement;

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

}

package it.albertus.cyclesmod.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BikeType {

	CLASS_125(125),
	CLASS_250(250),
	CLASS_500(500);

	private final int displacement;

	public static BikeType forDisplacement(final int displacement) {
		for (final BikeType bikeType : values()) {
			if (bikeType.displacement == displacement) {
				return bikeType;
			}
		}
		return null;
	}

}

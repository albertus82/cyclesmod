package io.github.albertus82.cyclesmod.common.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Game {

	CYCLES("The Cycles: International Grand Prix Racing"),
	GPC("Grand Prix Circuit");

	@NonNull
	private final String name;

}

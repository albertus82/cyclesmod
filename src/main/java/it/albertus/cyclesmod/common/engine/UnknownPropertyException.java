package it.albertus.cyclesmod.common.engine;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class UnknownPropertyException extends Exception {

	private static final long serialVersionUID = 8358143057948555987L;

	private final String name;

	public UnknownPropertyException(@NonNull final String name) {
		super("Unknown property name: " + name);
		this.name = name;
	}

}

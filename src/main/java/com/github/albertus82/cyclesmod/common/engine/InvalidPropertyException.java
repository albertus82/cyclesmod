package com.github.albertus82.cyclesmod.common.engine;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InvalidPropertyException extends Exception {

	private static final long serialVersionUID = -3070582022827873L;

	@NonNull
	private final String propertyName;

	public InvalidPropertyException(@NonNull final String propertyName, final String message, final Throwable cause) {
		super(message, cause);
		this.propertyName = propertyName;
	}

	public InvalidPropertyException(@NonNull final String propertyName, final String message) {
		super(message);
		this.propertyName = propertyName;
	}

}

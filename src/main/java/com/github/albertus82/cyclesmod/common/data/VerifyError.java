package com.github.albertus82.cyclesmod.common.data;

public class VerifyError extends Error {

	private static final long serialVersionUID = 6898720848930666950L;

	VerifyError(final String message, final Throwable cause) {
		super(message, cause);
	}

	VerifyError(final String message) {
		super(message);
	}

}

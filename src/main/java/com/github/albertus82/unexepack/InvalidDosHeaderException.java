package com.github.albertus82.unexepack;

public class InvalidDosHeaderException extends InvalidHeaderException {

	private static final long serialVersionUID = 8446610602802043780L;

	InvalidDosHeaderException(final byte[] headerBytes) {
		super(headerBytes);
	}

}

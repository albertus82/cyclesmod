package it.albertus.unexepack;

public class InvalidDosHeaderException extends InvalidHeaderException {

	private static final long serialVersionUID = 8446610602802043780L;

	public InvalidDosHeaderException(final byte[] headerBytes) {
		super(headerBytes);
	}

}

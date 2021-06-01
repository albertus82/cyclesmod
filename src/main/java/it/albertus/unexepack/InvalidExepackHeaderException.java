package it.albertus.unexepack;

public class InvalidExepackHeaderException extends InvalidHeaderException {

	private static final long serialVersionUID = -1424734787160373924L;

	InvalidExepackHeaderException(final byte[] headerBytes) {
		super(headerBytes);
	}

}

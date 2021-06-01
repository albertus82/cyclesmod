package it.albertus.unexepack;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class InvalidHeaderException extends Exception {

	private static final long serialVersionUID = -7116701588107760281L;

	private final byte[] headerBytes;

}

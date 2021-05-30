package it.albertus.unexepack;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InvalidHeaderException extends Exception {

	private static final long serialVersionUID = -7116701588107760281L;

	private final byte[] headerBytes;

}

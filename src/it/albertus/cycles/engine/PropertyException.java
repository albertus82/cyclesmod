package it.albertus.cycles.engine;

public class PropertyException extends RuntimeException {

	private static final long serialVersionUID = 2652422101698021597L;

	public PropertyException() {
		super();
	}

	public PropertyException(String message) {
		super(message);
	}

	public PropertyException(String message, Throwable cause) {
		super(message, cause);
	}

	public PropertyException(Throwable cause) {
		super(cause);
	}

}
package it.albertus.cycles.engine;

public class PropertyException extends RuntimeException {

	private static final long serialVersionUID = 2657462101698021597L;

	public PropertyException() {
		super();
	}

	public PropertyException( final String message ) {
		super( message );
	}

	public PropertyException( final String message, final Throwable cause) {
		super( message, cause );
	}

	public PropertyException( final Throwable cause) {
		super( cause );
	}

}
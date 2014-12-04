package it.albertus.cycles.engine;

public class InvalidPropertyException extends RuntimeException {

	private static final long serialVersionUID = 2657462101698021597L;

	public InvalidPropertyException() {
		super();
	}

	public InvalidPropertyException( final String message ) {
		super( message );
	}

	public InvalidPropertyException( final String message, final Throwable cause) {
		super( message, cause );
	}

	public InvalidPropertyException( final Throwable cause) {
		super( cause );
	}

}
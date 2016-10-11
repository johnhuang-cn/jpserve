package net.xdevelop.jpclient;

/**
 * Define a simple exception for this package
 */
public class PyServeException extends Exception {

	private static final long serialVersionUID = 2316314533629048927L;

	public PyServeException() {
		super();
	}

	public PyServeException(String message, Throwable cause) {
		super(message, cause);
	}

	public PyServeException(String message) {
		super(message);
	}

	public PyServeException(Throwable cause) {
		super(cause);
	}
}

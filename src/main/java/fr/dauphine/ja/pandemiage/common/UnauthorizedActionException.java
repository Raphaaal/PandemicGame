package fr.dauphine.ja.pandemiage.common;

public class UnauthorizedActionException extends Exception {
	private static final long serialVersionUID = 5818612515970836887L;

	public UnauthorizedActionException() {
		this("Unspecified reason.");
	}
	
	public UnauthorizedActionException(String msg) {
		super(msg);
	}

}

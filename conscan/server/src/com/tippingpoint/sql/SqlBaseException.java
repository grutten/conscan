package com.tippingpoint.sql;

/**
 * This class is the base class for SQL based exceptions.
 */
public class SqlBaseException extends Exception {
	private static final long serialVersionUID = 3702450890448779530L;

	/**
	 * This method constructs a new exception with the given message.
	 * 
	 * @param strMessage
	 */
	public SqlBaseException(final String strMessage) {
		super(strMessage);
	}

	/**
	 * This method constructs a new exception with the given related exception.
	 * 
	 * @param t Throwable representing the real exception.
	 */
	public SqlBaseException(final Throwable t) {
		super(t);
	}
}

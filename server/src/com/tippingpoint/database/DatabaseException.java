package com.tippingpoint.database;

/**
 * This class is a generic database/schema exception.
 */
public class DatabaseException extends Exception {
	private static final long serialVersionUID = 9061267630656270684L;

	/**
	 * This method constructs a new exception for schema generation.
	 * 
	 * @param strMessage
	 */
	public DatabaseException(final String strMessage) {
		super(strMessage);
	}
}

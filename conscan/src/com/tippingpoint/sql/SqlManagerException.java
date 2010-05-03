package com.tippingpoint.sql;

public class SqlManagerException extends Exception {
	private static final long serialVersionUID = -7492457566079788886L;

	/**
	 * This method constructs an exception with the given message.
	 */
	public SqlManagerException(final String strMessage) {
		super(strMessage);
	}
}

package com.tippingpoint.sql;

/**
 * SqlBuilderException
 *
 * This class is a generic SQL Builder exception.
 */
public final class SqlBuilderException extends Exception {
	private static final long serialVersionUID = 4961386323076181515L;

	/**
	 * This method constructs an exception with the given message.
	 */
	public SqlBuilderException(String strMessage) {
		super(strMessage);
	}
}

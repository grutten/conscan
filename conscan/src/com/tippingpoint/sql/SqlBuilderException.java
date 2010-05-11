package com.tippingpoint.sql;

/**
 * This class is a generic SQL Builder exception.
 */
public final class SqlBuilderException extends SqlBaseException {
	private static final long serialVersionUID = 4961386323076181515L;

	/**
	 * This method constructs an exception with the given message.
	 */
	public SqlBuilderException(final String strMessage) {
		super(strMessage);
	}
}

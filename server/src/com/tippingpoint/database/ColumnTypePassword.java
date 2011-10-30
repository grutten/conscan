package com.tippingpoint.database;

public class ColumnTypePassword extends ColumnTypeString {
	public static final String TYPE = "password";

	/**
	 * This method constructs a new column type.
	 */
	protected ColumnTypePassword() {
		super(TYPE);
	}
}

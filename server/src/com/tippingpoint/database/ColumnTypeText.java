package com.tippingpoint.database;

/**
 * This class represents a large, string type element (i.e. a CLOB interpreted as a string).
 */
public final class ColumnTypeText extends ColumnTypeString {
	public static final String TYPE = "text";

	/**
	 * This method constructs a new column type.
	 */
	protected ColumnTypeText() {
		super(TYPE);
	}

	/**
	 * This method is used to indicate that a length is associated with this column type.
	 */
	@Override
	public boolean hasLength() {
		return false;
	}
}

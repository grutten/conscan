package com.tippingpoint.database;

/**
 * This class represents a column type of reference to an ID.
 */
public class ColumnTypeIdReference extends ColumnTypeIdBase {
	public static final String TYPE = "idref";

	/**
	 * This method constructs a new column type.
	 */
	protected ColumnTypeIdReference() {
		super(TYPE);
	}
}

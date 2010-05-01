package com.tippingpoint.database;

/**
 * This class provides a common base for the ID related column types.
 */
abstract class ColumnTypeIdBase extends ColumnTypeIdentifierBase {
	/**
	 * This method constructs a new identifier type column type.
	 */
	protected ColumnTypeIdBase(final String strType) {
		super(strType);
	}

	/**
	 * This method interprets the object returned from a ResultSet and translates it into an appropriate object.
	 */
	@Override
	protected Object translateObject(final int nValue, final boolean bWasNull) {
		return !bWasNull ? new Id(new Integer(nValue)) : null;
	}
}
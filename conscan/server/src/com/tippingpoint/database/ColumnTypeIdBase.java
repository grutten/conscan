package com.tippingpoint.database;

import com.tippingpoint.sql.ConnectionManagerFactory;

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
	protected Object translateObject(final String strValue, final boolean bWasNull) {
		return !bWasNull ? new Id(strValue) : null;
	}

	/**
	 * This method gives a changes for the type to set any restrictions on the column type.
	 * 
	 * @param columnDefinition ColumnDefinition being defined by this type.
	 */
	@Override
	public void setRestrictions(final ColumnDefinition columnDefinition) {
		super.setRestrictions(columnDefinition);

		final IdFactory idFactory = ConnectionManagerFactory.getFactory().getDefaultManager().getIdFactory();
		if (idFactory.hasLength()) {
			columnDefinition.setLength(idFactory.getLength());
		}
	}
}

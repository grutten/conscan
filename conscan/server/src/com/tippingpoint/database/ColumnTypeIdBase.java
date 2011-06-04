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
	 * This returns the length associated with the type.
	 */
	@Override
	public int getLength() {
		int nLength = -1;
		final IdFactory idFactory = ConnectionManagerFactory.getFactory().getDefaultManager().getIdFactory();
		if (idFactory.hasLength()) {
			nLength = idFactory.getLength();
		}

		return nLength;
	}

	/**
	 * This method returns if the type defines the length of the field.
	 */
	@Override
	public boolean isLengthSetByType() {
		final IdFactory idFactory = ConnectionManagerFactory.getFactory().getDefaultManager().getIdFactory();
		return idFactory.hasLength();
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

	/**
	 * This method interprets the object returned from a ResultSet and translates it into an appropriate object.
	 */
	@Override
	protected Object translateObject(final String strValue, final boolean bWasNull) {
		return !bWasNull ? new Id(strValue) : null;
	}
}

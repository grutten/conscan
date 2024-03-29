package com.tippingpoint.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.tippingpoint.sql.ConnectionManagerFactory;

/**
 * This class is used for a base class for both ids and sequences. Since we currently only have integer representations
 * of both of these classes, then the majority of the implementation between the ids and sequences are the same.
 */
abstract class ColumnTypeIdentifierBase extends ColumnType {
	/**
	 * This method constructs a new identifier type column type.
	 */
	protected ColumnTypeIdentifierBase(final String strType) {
		super(strType);
	}

	/**
	 * This method returns the JDBC Type, used for setting null.
	 */
	@Override
	public int getJdbcType() {
		return ConnectionManagerFactory.getFactory().getDefaultManager().getIdFactory().getJdbcType();
	}

	/**
	 * This method returns an object that represents the value found in the result set.
	 * 
	 * @throws SQLException
	 */
	@Override
	public Object getResult(final ResultSet rs, final int nIndex) throws SQLException {
		return translateObject(rs.getString(nIndex), rs.wasNull());
	}

	/**
	 * This method is used to indicate that a length is associated with this column type.
	 */
	@Override
	public boolean hasLength() {
		return ConnectionManagerFactory.getFactory().getDefaultManager().getIdFactory().hasLength();
	}

	/**
	 * This method interprets the object returned from a ResultSet and translates it into an appropriate object.
	 */
	protected abstract Object translateObject(String strValue, boolean bWasNull);
}
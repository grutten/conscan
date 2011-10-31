package com.tippingpoint.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ColumnTypeSmallInteger
 */
public class ColumnTypeSmallInteger extends ColumnType {
	public static final String TYPE = "smallinteger";

	/**
	 * This method is the default constructor, which represents the small integer type of column.
	 */
	protected ColumnTypeSmallInteger() {
		super(TYPE);
	}

	/**
	 * This method returns the JDBC Type, used for setting null.
	 */
	@Override
	public int getJdbcType() {
		return Types.SMALLINT;
	}

	/**
	 * This method returns an object that represents the value found in the result set.
	 * 
	 * @throws SQLException
	 */
	@Override
	public Object getResult(final ResultSet rs, final int nIndex) throws SQLException {
		final int nValue = rs.getInt(nIndex);

		return !rs.wasNull() ? new Integer(nValue) : null;
	}
}

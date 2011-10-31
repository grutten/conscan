package com.tippingpoint.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * This class represents a column of type integer.
 */
public class ColumnTypeInteger extends ColumnType {
	public static final String TYPE = "integer";

	/**
	 * This method is the default constructor, which represents the integer type of column.
	 */
	protected ColumnTypeInteger() {
		super(TYPE);
	}

	/**
	 * This method returns the JDBC Type, used for setting null.
	 */
	@Override
	public int getJdbcType() {
		return Types.INTEGER;
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

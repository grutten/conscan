package com.tippingpoint.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * This class represents a column of type date.
 */
public class ColumnTypeBoolean extends ColumnType {
	public static final String TYPE = "boolean";

	/**
	 * This method is the default constructor, which represents the boolean type of column.
	 */
	protected ColumnTypeBoolean() {
		super(TYPE);
	}

	/**
	 * This method returns the JDBC Type, used for setting null.
	 */
	@Override
	public int getJdbcType() {
		return Types.BOOLEAN;
	}

	/**
	 * This method returns an object that represents the value found in the result set.
	 * 
	 * @throws SQLException
	 */
	@Override
	public Object getResult(final ResultSet rs, final int nIndex) throws SQLException {
		final boolean bValue = rs.getBoolean(nIndex);

		return bValue;
	}
}

package com.tippingpoint.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

/**
 * This class represents a column of type date.
 */
public final class ColumnTypeDate extends ColumnType {
	public static final String TYPE = "date";

	/**
	 * This method is the default constructor, which represents the timestamp type of column.
	 */
	protected ColumnTypeDate() {
		super(TYPE);
	}

	/**
	 * This method returns the JDBC Type, used for setting null.
	 */
	@Override
	public int getJdbcType() {
		return Types.TIMESTAMP;
	}

	/**
	 * This method returns an object that represents the value found in the result set.
	 * 
	 * @throws SQLException
	 */
	@Override
	public Object getResult(final ResultSet rs, final int nIndex) throws SQLException {
		final Timestamp dtValue = rs.getTimestamp(nIndex);

		return dtValue != null ? new Date(dtValue.getTime()) : null;
	}
}

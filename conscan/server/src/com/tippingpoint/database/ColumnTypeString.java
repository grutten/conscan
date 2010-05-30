package com.tippingpoint.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ColumnTypeString
 */
public class ColumnTypeString extends ColumnType {
	public static final String TYPE = "string";

	/**
	 * This method constructs a new column type.
	 */
	protected ColumnTypeString() {
		super(TYPE);
	}

	/**
	 * This method constructs a new string column type.
	 */
	protected ColumnTypeString(final String strType) {
		super(strType);
	}

	/**
	 * This method returns the JDBC Type, used for setting null.
	 */
	@Override
	public int getJdbcType() {
		return Types.VARCHAR;
	}

	/**
	 * This method returns an object that represents the value found in the result set.
	 * 
	 * @throws SQLException
	 */
	@Override
	public Object getResult(final ResultSet rs, final int nIndex) throws SQLException {
		return rs.getString(nIndex);
	}

	/**
	 *
	 */
	@Override
	public boolean hasLength() {
		return true;
	}
}

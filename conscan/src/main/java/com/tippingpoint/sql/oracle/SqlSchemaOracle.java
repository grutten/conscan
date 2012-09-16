package com.tippingpoint.sql.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.base.SqlManager;
import com.tippingpoint.sql.base.SqlSchema;

/**
 * This class is used to read in the schema from the Oracle database.
 */
public class SqlSchemaOracle extends SqlSchema {
	private static final String SQL_COLUMN = "SELECT COLUMN_NAME, DATA_DEFAULT COLUMN_DEFAULT, NULLABLE IS_NULLABLE, NULL "
			+ "ID_COLUMN, DATA_TYPE, DATA_LENGTH CHARACTER_MAXIMUM_LENGTH, COLUMN_ID ORDINAL_POSITION FROM USER_TAB_COLUMNS "
			+ "WHERE TABLE_NAME = ? ORDER BY COLUMN_ID";
	private static final String SQL_TABLE = "select TABLE_NAME from user_tables";

	/**
	 * This method constructs a new schema for the given manager.
	 */
	public SqlSchemaOracle(final SqlManager sqlManager) {
		super(sqlManager);
	}

	/**
	 * This method returns the SQL used to return the table information.
	 * 
	 * @throws SQLException
	 */
	@Override
	protected PreparedStatement getSchemaStatement(final Connection conn, final Schema schema) throws SQLException {
		final PreparedStatement pstmt = conn.prepareStatement(SQL_TABLE);

		return pstmt;
	}

	/**
	 * This method returns the SQL used to return the column information.
	 * 
	 * @throws SQLException
	 */
	@Override
	protected PreparedStatement getTableStatement(final Connection conn, final Schema schema, final Table table)
			throws SQLException {
		final PreparedStatement pstmt = conn.prepareStatement(SQL_COLUMN);
		pstmt.setString(1, table.getName());

		return pstmt;
	}
}

package com.tippingpoint.sql.sqlserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.base.SqlManager;
import com.tippingpoint.sql.base.SqlSchema;

/**
 * This class is used to read in the schema from the SQL Server database.
 */
public class SqlSchemaSqlServer extends SqlSchema {
	private static final String COLUMN_SQL =
		"SELECT COLUMN_NAME, COLUMN_DEFAULT, IS_NULLABLE, "
				+ "COLUMNPROPERTY(OBJECT_ID(TABLE_SCHEMA + '.' + TABLE_NAME), COLUMN_NAME, 'IsIdentity') ID_COLUMN, "
				+ "DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, ORDINAL_POSITION FROM INFORMATION_SCHEMA.COLUMNS "
				+ "WHERE TABLE_CATALOG = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";

	/**
	 * This method constructs a new schema for the given manager.
	 */
	public SqlSchemaSqlServer(final SqlManager sqlManager) {
		super(sqlManager);
	}

	/**
	 * This method returns the SQL used to return the column information.
	 * 
	 * @throws SQLException
	 */
	@Override
	protected PreparedStatement getTableStatement(final Connection conn, final Schema schema, final Table table)
			throws SQLException {
		final PreparedStatement pstmt = conn.prepareStatement(COLUMN_SQL);
		pstmt.setString(1, schema.getName());
		pstmt.setString(2, table.getName());

		return pstmt;
	}
}

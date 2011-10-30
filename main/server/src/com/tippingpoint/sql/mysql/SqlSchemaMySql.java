package com.tippingpoint.sql.mysql;

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
public class SqlSchemaMySql extends SqlSchema {
	private static final String SQL_COLUMN =
		"SELECT COLUMN_NAME, COLUMN_DEFAULT, IS_NULLABLE, IF(STRCMP(EXTRA,'auto_increment'),0,1) ID_COLUMN, "
				+ "DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, ORDINAL_POSITION FROM INFORMATION_SCHEMA.COLUMNS "
				+ "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
	private static final String SQL_FOREIGN_KEYS =
		"SELECT kcu.CONSTRAINT_NAME FK_CONSTRAINT_NAME, kcu.TABLE_NAME FK_TABLE_NAME, kcu.COLUMN_NAME FK_COLUMN_NAME, "
				+ "kcu.ORDINAL_POSITION FK_ORDINAL_POSITION, kcu.REFERENCED_TABLE_NAME PK_TABLE_NAME, "
				+ "kcu.REFERENCED_COLUMN_NAME PK_COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu, "
				+ "INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc WHERE kcu.TABLE_SCHEMA = ? AND kcu.TABLE_NAME = ? AND "
				+ "kcu.TABLE_SCHEMA = tc.TABLE_SCHEMA AND kcu.TABLE_NAME = tc.TABLE_NAME AND kcu.CONSTRAINT_NAME = "
				+ "tc.CONSTRAINT_NAME AND tc.CONSTRAINT_TYPE = 'FOREIGN KEY' ORDER BY kcu.TABLE_SCHEMA, "
				+ "kcu.TABLE_NAME, kcu.ORDINAL_POSITION";
	private static final String SQL_KEYS =
		"SELECT kcu.CONSTRAINT_NAME, tc.CONSTRAINT_TYPE, kcu.COLUMN_NAME, kcu.ORDINAL_POSITION FROM "
				+ "INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc, INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu WHERE "
				+ "tc.CONSTRAINT_TYPE != 'FOREIGN KEY' AND tc.TABLE_SCHEMA = ? AND tc.TABLE_NAME = ? AND "
				+ "tc.CONSTRAINT_SCHEMA = kcu.CONSTRAINT_SCHEMA AND tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME AND "
				+ "tc.TABLE_SCHEMA = kcu.TABLE_SCHEMA AND tc.TABLE_NAME = kcu.TABLE_NAME ORDER BY tc.CONSTRAINT_TYPE,"
				+ "kcu.CONSTRAINT_NAME, kcu.ORDINAL_POSITION";
	private static final String SQL_TABLE = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?";

	/**
	 * This method constructs a new schema for the given manager.
	 */
	public SqlSchemaMySql(final SqlManager sqlManager) {
		super(sqlManager);
	}

	/**
	 * This method returns the SQL used to return the foreign key column information.
	 * 
	 * @throws SQLException
	 */
	@Override
	protected PreparedStatement getForeignKeyStatement(final Connection conn, final Schema schema, final Table table)
			throws SQLException {
		final PreparedStatement pstmt = conn.prepareStatement(SQL_FOREIGN_KEYS);
		pstmt.setString(1, schema.getName());
		pstmt.setString(2, table.getName());

		return pstmt;
	}

	/**
	 * This method returns the SQL used to return the key information.
	 * 
	 * @throws SQLException
	 */
	@Override
	protected PreparedStatement getKeyStatement(final Connection conn, final Schema schema, final Table table)
			throws SQLException {
		final PreparedStatement pstmt = conn.prepareStatement(SQL_KEYS);
		pstmt.setString(1, schema.getName());
		pstmt.setString(2, table.getName());

		return pstmt;
	}

	/**
	 * This method returns the SQL used to return the table information.
	 * 
	 * @throws SQLException
	 */
	@Override
	protected PreparedStatement getSchemaStatement(final Connection conn, final Schema schema) throws SQLException {
		final PreparedStatement pstmt = conn.prepareStatement(SQL_TABLE);
		pstmt.setString(1, schema.getName());

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
		pstmt.setString(1, schema.getName());
		pstmt.setString(2, table.getName());

		return pstmt;
	}
}

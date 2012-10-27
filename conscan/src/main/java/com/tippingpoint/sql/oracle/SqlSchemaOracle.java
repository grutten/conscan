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
	private static final String SQL_COLUMN =
		"SELECT COLUMN_NAME, DATA_DEFAULT COLUMN_DEFAULT, decode(NULLABLE, 'Y', 'YES', 'NO') IS_NULLABLE, NULL ID_COLUMN, "
				+ "DATA_TYPE, DATA_LENGTH CHARACTER_MAXIMUM_LENGTH, COLUMN_ID ORDINAL_POSITION FROM USER_TAB_COLUMNS "
				+ "WHERE TABLE_NAME = ? ORDER BY COLUMN_ID";
	private static final String SQL_FOREIGN_KEYS =
		"SELECT ucc.constraint_name FK_CONSTRAINT_NAME, ucc.table_name FK_TABLE_NAME, ucc.column_name FK_COLUMN_NAME, "
				+ "ucc.position FK_ORDINAL_POSITION, ucc2.constraint_name PK_CONSTRAINT_NAME, ucc2.table_name PK_TABLE_NAME, "
				+ "ucc2.column_name PK_COLUMN_NAME, ucc2.position PK_ORDINAL_POSITION FROM user_constraints uc, "
				+ "user_cons_columns ucc, user_cons_columns ucc2 WHERE uc.table_name = ucc.table_name AND uc.constraint_name = "
				+ "ucc.constraint_name AND ucc.table_name = ? AND uc.constraint_type = 'R' AND uc.status = 'ENABLED' AND "
				+ "uc.R_CONSTRAINT_NAME = ucc2.constraint_name AND ucc2.position = ucc.position";
	private static final String SQL_KEYS =
		"SELECT uc.CONSTRAINT_NAME, decode(uc.constraint_type, 'P', 'PRIMARY KEY', 'R', 'Foreign Key', 'C', 'Check', 'UNIQUE')"
				+ " as CONSTRAINT_TYPE, ucc.COLUMN_NAME, ucc.POSITION ORDINAL_POSITION FROM USER_CONSTRAINTS uc, "
				+ "USER_CONS_COLUMNS ucc WHERE uc.constraint_type != 'R' AND SUBSTR(uc.CONSTRAINT_NAME, 1, 4) != 'SYS_' AND "
				+ "uc.CONSTRAINT_NAME = ucc.CONSTRAINT_NAME AND uc.TABLE_NAME = ? " + "ORDER BY ORDINAL_POSITION";
	private static final String SQL_TABLE = "select TABLE_NAME from user_tables";

	/**
	 * This method constructs a new schema for the given manager.
	 */
	public SqlSchemaOracle(final SqlManager sqlManager) {
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
		pstmt.setString(1, table.getName().toUpperCase()); // upper case since Oracle does this on creation

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
		pstmt.setString(1, table.getName().toUpperCase()); // upper case since Oracle does this on creation

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
		pstmt.setString(1, table.getName().toUpperCase()); // upper case since Oracle does this on creation

		return pstmt;
	}
}

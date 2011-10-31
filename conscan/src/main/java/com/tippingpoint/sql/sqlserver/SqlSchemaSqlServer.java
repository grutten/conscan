package com.tippingpoint.sql.sqlserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.DbUtils;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.ColumnTypeBoolean;
import com.tippingpoint.database.ColumnTypeFactory;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.SqlExecutionException;
import com.tippingpoint.sql.base.SqlManager;
import com.tippingpoint.sql.base.SqlSchema;

/**
 * This class is used to read in the schema from the SQL Server database.
 */
public class SqlSchemaSqlServer extends SqlSchema {
	private static final String SQL_CHECK =
		"SELECT cc.CONSTRAINT_NAME, CHECK_CLAUSE, COLUMN_NAME FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS cc, "
				+ "INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu WHERE cc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME AND "
				+ "ccu.TABLE_CATALOG = ? AND ccu.TABLE_NAME = ?";
	private static final String SQL_COLUMN =
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
		final PreparedStatement pstmt = conn.prepareStatement(SQL_COLUMN);
		pstmt.setString(1, schema.getName());
		pstmt.setString(2, table.getName());

		return pstmt;
	}

	/**
	 * This method reads in the information about the named table.
	 * 
	 * @param table Table definition which contains the name of the Table.
	 * @throws SqlExecutionException
	 */
	@Override
	protected void readTable(final Connection conn, final Schema schema, final Table table)
			throws SqlExecutionException {
		super.readTable(conn, schema, table);

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		// determine if there are any check constraints indicating boolean fields
		try {
			pstmt = conn.prepareStatement(SQL_CHECK);
			pstmt.setString(1, schema.getName());
			pstmt.setString(2, table.getName());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				final String strConstraintName = rs.getString("CONSTRAINT_NAME");
				final String strColumnName = rs.getString("COLUMN_NAME");

				if (strConstraintName.startsWith(SqlManager.BOOLEAN_CHECK_PREFIX)) {
					final ColumnDefinition column = table.getColumn(strColumnName);
					column.setType(ColumnTypeFactory.getFactory().get(ColumnTypeBoolean.TYPE));
				}
			}
		}
		catch (final SQLException e) {
			throw new SqlExecutionException(SQL_CHECK, e);
		}
		finally {
			DbUtils.closeQuietly(null, pstmt, rs);
		}
	}
}

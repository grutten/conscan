package com.tippingpoint.sql.base;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.dbutils.DbUtils;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.ColumnTypeFactory;
import com.tippingpoint.database.ColumnTypeId;
import com.tippingpoint.database.ColumnTypeIdReference;
import com.tippingpoint.database.ColumnTypeInteger;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.ConstraintFactory;
import com.tippingpoint.database.DatabaseElementException;
import com.tippingpoint.database.ForeignKey;
import com.tippingpoint.database.ForeignKeyConstraint;
import com.tippingpoint.database.Index;
import com.tippingpoint.database.LogicalKeyConstraint;
import com.tippingpoint.database.PrimaryKeyConstraint;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.SqlExecutionException;

/**
 * This class is used to read in the schema from the database.
 */
public class SqlSchema {
	/** This map holds the mapping of database constraint types. */
	private static Map<String, String> m_mapConstraintName = new HashMap<String, String>();

	private static final String SQL_COLUMN =
		"SELECT COLUMN_NAME, COLUMN_DEFAULT, IS_NULLABLE, NULL ID_COLUMN, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, "
				+ "ORDINAL_POSITION FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_CATALOG = ? AND TABLE_NAME = ? "
				+ "ORDER BY ORDINAL_POSITION";
	private static final String SQL_FOREIGN_KEYS =
		"SELECT kcu1.CONSTRAINT_NAME FK_CONSTRAINT_NAME, kcu1.TABLE_NAME FK_TABLE_NAME, "
				+ "kcu1.COLUMN_NAME FK_COLUMN_NAME, kcu1.ORDINAL_POSITION FK_ORDINAL_POSITION, "
				+ "kcu2.CONSTRAINT_NAME PK_CONSTRAINT_NAME, kcu2.TABLE_NAME PK_TABLE_NAME, "
				+ "kcu2.COLUMN_NAME PK_COLUMN_NAME, kcu2.ORDINAL_POSITION PK_ORDINAL_POSITION "
				+ "FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS rc, INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu1, "
				+ "INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu2 WHERE kcu1.TABLE_CATALOG = ? AND kcu1.TABLE_NAME = ? AND "
				+ "kcu1.CONSTRAINT_CATALOG = rc.CONSTRAINT_CATALOG AND kcu1.CONSTRAINT_SCHEMA = rc.CONSTRAINT_SCHEMA "
				+ "AND kcu1.CONSTRAINT_NAME = rc.CONSTRAINT_NAME AND kcu2.CONSTRAINT_CATALOG = "
				+ "rc.UNIQUE_CONSTRAINT_CATALOG AND kcu2.CONSTRAINT_SCHEMA = rc.UNIQUE_CONSTRAINT_SCHEMA AND "
				+ "kcu2.CONSTRAINT_NAME = rc.UNIQUE_CONSTRAINT_NAME AND kcu2.ORDINAL_POSITION = kcu1.ORDINAL_POSITION";
	private static final String SQL_KEYS =
		"SELECT kcu.CONSTRAINT_NAME, tc.CONSTRAINT_TYPE, kcu.COLUMN_NAME, kcu.ORDINAL_POSITION FROM "
				+ "INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc, INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu WHERE "
				+ "tc.CONSTRAINT_TYPE != 'FOREIGN KEY' AND tc.TABLE_CATALOG = ? AND tc.TABLE_NAME = ? AND "
				+ "tc.CONSTRAINT_SCHEMA = kcu.CONSTRAINT_SCHEMA AND tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME AND "
				+ "tc.TABLE_SCHEMA = kcu.TABLE_SCHEMA AND tc.TABLE_NAME = kcu.TABLE_NAME ORDER BY tc.CONSTRAINT_TYPE, "
				+ "kcu.CONSTRAINT_NAME, kcu.ORDINAL_POSITION";
	private static final String SQL_TABLE = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_CATALOG = ?";

	/** This member holds the manager associated with this schema. */
	private final WeakReference<SqlManager> m_sqlManager;

	/**
	 * This method constructs a new schema for the given manager.
	 */
	public SqlSchema(final SqlManager sqlManager) {
		m_sqlManager = new WeakReference<SqlManager>(sqlManager);
	}

	/**
	 * This method will return a schema based on the details found in the database.
	 * 
	 * @throws SqlExecutionException
	 * @throws DatabaseElementException
	 */
	public Schema getSchema(final Connection conn, final String strSchemaName) throws SqlExecutionException,
			DatabaseElementException {
		final Schema schema = new Schema(strSchemaName);

		readSchema(conn, schema);

		return schema;
	}

	/**
	 * This method returns the SQL used to return the foreign key column information.
	 * 
	 * @throws SQLException
	 */
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
	protected void readTable(final Connection conn, final Schema schema, final Table table)
			throws SqlExecutionException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = getTableStatement(conn, schema, table);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				final String strColumnName = rs.getString("COLUMN_NAME");
				final String strDefault = rs.getString("COLUMN_DEFAULT");
				final boolean bNullable = "YES".equals(rs.getString("IS_NULLABLE"));
				final boolean bIdColumn = rs.getBoolean("ID_COLUMN");
				final String strDataType = rs.getString("DATA_TYPE");
				final int nColumnSize = rs.getInt("CHARACTER_MAXIMUM_LENGTH");

				final ColumnDefinition column =
					new ColumnDefinition(table, strColumnName, m_sqlManager.get().getType(strDataType, bIdColumn));

				if (column.getType().hasLength()) {
					column.setLength(nColumnSize);
				}

				column.setRequired(!bNullable);
				column.setDefault(strDefault);
			}
		}
		catch (final SQLException e) {
			throw new SqlExecutionException(SQL_COLUMN, e);
		}
		finally {
			DbUtils.closeQuietly(null, pstmt, rs);
		}
	}

	/**
	 * This method reads in the information about the schema from the database.
	 * 
	 * @throws SqlExecutionException
	 * @throws DatabaseElementException
	 */
	private void readSchema(final Connection conn, final Schema schema) throws SqlExecutionException,
			DatabaseElementException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = getSchemaStatement(conn, schema);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				final String strTableName = rs.getString("TABLE_NAME");

				new Table(schema, strTableName);
			}
		}
		catch (final SQLException e) {
			throw new SqlExecutionException(SQL_TABLE, e);
		}
		finally {
			DbUtils.closeQuietly(null, pstmt, rs);
		}

		Iterator<Table> iterTables = schema.getTables();
		if (iterTables != null && iterTables.hasNext()) {
			while (iterTables.hasNext()) {
				final Table table = iterTables.next();

				readTable(conn, schema, table);
				readTableKeys(conn, schema, table);
			}
		}

		// iterate again to read in the foreign keys since the table columns are needed
		iterTables = schema.getTables();
		if (iterTables != null && iterTables.hasNext()) {
			while (iterTables.hasNext()) {
				final Table table = iterTables.next();

				readTableForeignKeys(conn, schema, table);
			}
		}
	}

	/**
	 * This method reads the tables foreign key constraints.
	 * 
	 * @throws SqlExecutionException
	 * @throws DatabaseElementException
	 */
	private void readTableForeignKeys(final Connection conn, final Schema schema, final Table table)
			throws SqlExecutionException, DatabaseElementException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = getForeignKeyStatement(conn, schema, table);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				final String strConstraintName = rs.getString("FK_CONSTRAINT_NAME");
				final String strChildColumnName = rs.getString("FK_COLUMN_NAME");
				final String strParentTableName = rs.getString("PK_TABLE_NAME");
				final String strParentColumnName = rs.getString("PK_COLUMN_NAME");

				Constraint constraint = table.getConstraint(strConstraintName);
				if (constraint == null) {
					constraint = new ForeignKeyConstraint();

					constraint.setName(strConstraintName);

					table.add(constraint);
				}

				// add the column to the constraint
				final ForeignKey foreignKey = new ForeignKey();

				final ColumnDefinition columnChild = table.getColumn(strChildColumnName);
				foreignKey.setChildColumn(columnChild);

				final Table tableParent = schema.getTable(strParentTableName);
				final Column columnParent = tableParent.getColumn(strParentColumnName);
				foreignKey.setParentColumn(columnParent);

				constraint.addColumn(foreignKey);

				// since a reference to an ID column can not be determined based on the type of the
				// database column; a foreign key to an ID column can be used to change an integer
				// column to and idref column type
				if (ColumnTypeInteger.class.equals(columnChild.getType().getClass()) &&
						ColumnTypeId.class.equals(columnParent.getType().getClass())) {
					columnChild.setType(ColumnTypeFactory.getFactory().get(ColumnTypeIdReference.TYPE));
				}
			}
		}
		catch (final SQLException e) {
			throw new SqlExecutionException(SQL_FOREIGN_KEYS, e);
		}
		finally {
			DbUtils.closeQuietly(null, pstmt, rs);
		}
	}

	/**
	 * This method reads the tables primary key constraints.
	 * 
	 * @throws SqlExecutionException
	 * @throws DatabaseElementException
	 */
	private void readTableKeys(final Connection conn, final Schema schema, final Table table)
			throws SqlExecutionException, DatabaseElementException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = getKeyStatement(conn, schema, table);
			rs = pstmt.executeQuery();

			Constraint constraint = null;

			while (rs.next()) {
				final int nPosition = rs.getInt("ORDINAL_POSITION");
				final String strConstraintType = rs.getString("CONSTRAINT_TYPE");
				final String strColumnName = rs.getString("COLUMN_NAME");
				final String strKeyName = rs.getString("CONSTRAINT_NAME");

				if (nPosition == 1) {
					if (constraint != null) {
						table.add(constraint);
					}

					constraint = ConstraintFactory.getFactory().get(m_mapConstraintName.get(strConstraintType));
					if (constraint instanceof Index && strKeyName.startsWith("lk_")) {
						constraint = ConstraintFactory.getFactory().get(LogicalKeyConstraint.TYPE);
					}

					constraint.setName(strKeyName);
				}

				// add the column to the constraint
				constraint.addColumn(table.getColumn(strColumnName));
			}

			if (constraint != null) {
				table.add(constraint);
			}
		}
		catch (final SQLException e) {
			throw new SqlExecutionException(SQL_KEYS, e);
		}
		finally {
			DbUtils.closeQuietly(null, pstmt, rs);
		}
	}

	static {
		m_mapConstraintName.put("PRIMARY KEY", PrimaryKeyConstraint.TYPE);
		m_mapConstraintName.put("UNIQUE", Index.TYPE);
	}
}

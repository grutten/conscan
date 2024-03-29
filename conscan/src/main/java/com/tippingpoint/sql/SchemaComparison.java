package com.tippingpoint.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.ForeignKeyConstraint;
import com.tippingpoint.database.PrimaryKeyConstraint;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.base.SqlManager;

/**
 * This class is used to compare schemas and attempts to make the schemas similar.
 */
public final class SchemaComparison {
	private static final Log m_log = LogFactory.getLog(SchemaComparison.class);

	/** This member holds the schema of the desired database. */
	private final Schema m_schema;

	/** This member holds the schema of the database. */
	private final Schema m_schemaCurrent;

	/**
	 * This method constructs a new comparison class.
	 * 
	 * @param schemaCurrent Schema currently representing the database.
	 * @param schema Schema representing what the database should be like.
	 */
	public SchemaComparison(final Schema schemaCurrent, final Schema schema) {
		m_schemaCurrent = schemaCurrent;
		m_schema = schema;
	}

	/**
	 * This method processes initiates the comparison.
	 * 
	 * @param connectionManager ConnectionManager used to modify the database.
	 * @throws SQLException
	 * @throws SqlBaseException
	 */
	public void process(final ConnectionManager connectionManager) throws SQLException, SqlBaseException {
		Connection conn = null;
		final SqlManager sqlManager = connectionManager.getSqlManager();

		try {
			conn = connectionManager.getConnection();

			final Iterator<Table> iterTables = m_schema.getTables();
			while (iterTables.hasNext()) {
				final Table table = iterTables.next();

				if (m_schemaCurrent.hasTable(table.getName())) {
					compare(conn, sqlManager, m_schemaCurrent.getTable(table.getName()), table);
				}
				else {
					final SqlCreate sqlCreate = new SqlCreate(table);

					m_log.info("Adding table: " + sqlCreate.toString());

					sqlManager.executeUpdate(sqlCreate, conn);
				}
			}
		}
		finally {
			ConnectionManager.close(conn, null, null);
		}
	}

	/**
	 * This method is used to alter a table to add/modify the specified column.
	 * 
	 * @param conn Connection used for all database transactions
	 * @param sqlManager SqlManager used for all database transactions
	 * @param tableCurrent Table definition for the currently defined table
	 * @param column Column definition of the new column.
	 * @throws SqlBaseException
	 */
	private void alter(final Connection conn, final SqlManager sqlManager, final Table tableCurrent,
			final ColumnDefinition column) throws SqlBaseException {
		// if this is not a new column, then check for containing constraints
		final Column columnCurrent = tableCurrent.getColumn(column.getName());
		if (columnCurrent != null) {
			// the definition of the column is changing, which may block any constraints on that column; therefore,
			// remove the constraints prior to altering the column and they will be added back when the constraints are
			// checked
			final List<Constraint> listConstraints = tableCurrent.getConstraintList();
			for (int nIndex = 0; nIndex < listConstraints.size(); ++nIndex) {
				final Constraint constraint = listConstraints.get(nIndex);

				// because we currently can not identify MySQL auto increment columns, dropping the primary key causes a
				// problem; therefore, avoid dropping the primary key
				if (!(constraint instanceof PrimaryKeyConstraint)) {
					dropContainingConstraint(conn, sqlManager, tableCurrent, listConstraints.get(nIndex), columnCurrent);
				}
			}
		}

		final SqlAlter sqlAlter = new SqlAlter(tableCurrent);

		sqlAlter.add(column);

		m_log.info("Changing database with: " + sqlAlter.toString());

		sqlManager.executeUpdate(sqlAlter, conn);
	}

	/**
	 * This method is used to alter a table to add/modify the specified constraint.
	 * 
	 * @param conn Connection used for all database transactions
	 * @param sqlManager SqlManager used for all database transactions
	 * @param table Table definition for the currently defined table
	 * @param constraint Constraint definition of the new constraint.
	 * @throws SqlBaseException
	 */
	private void alter(final Connection conn, final SqlManager sqlManager, final Table table,
			final Constraint constraint) throws SqlBaseException {
		final SqlAlter sqlAlter = new SqlAlter(table);

		sqlAlter.add(constraint);

		m_log.info("Changing database with: " + sqlAlter.toString());

		sqlManager.executeUpdate(sqlAlter, conn);
	}

	/**
	 * This method compares constraints of the desired table vs. the current table. This is slightly less forward that
	 * expected as it is done by name since we are not completely able to determine types of keys (i.e. logical key).
	 * 
	 * @param conn Connection used for all database transactions
	 * @param sqlManager SqlManager used for all database transactions
	 * @param tableCurrent Table definition of the current table.
	 * @param constraint Constraint definition of the desired constraint.
	 * @throws SqlBaseException
	 */
	private void compare(final Connection conn, final SqlManager sqlManager, final Table tableCurrent,
			final Constraint constraint) throws SqlBaseException {
		if (constraint != null) {
			Constraint constraintCurrent = null;

			constraintCurrent = tableCurrent.getConstraint(constraint.getName());
			if (constraintCurrent != null) {
				if (!constraint.equals(constraintCurrent)) {
					if (constraint instanceof ForeignKeyConstraint) {
						// foreign keys can't be altered, so drop the old key and let the alter recreate it
						drop(conn, sqlManager, tableCurrent, constraintCurrent);
					}

					alter(conn, sqlManager, tableCurrent, constraint);
				}
			}
			else {
				alter(conn, sqlManager, tableCurrent, constraint);
			}
		}
	}

	/**
	 * This method compares primary key constraints of the desired table versus the current table.
	 * 
	 * @param conn Connection used for all database transactions
	 * @param sqlManager SqlManager used for all database transactions
	 * @param tableCurrent Table definition of the current table.
	 * @param primaryKeyConstraint PrimaryKeyConstraint definition of the desired constraint.
	 * @throws SqlBaseException
	 */
	private void compare(final Connection conn, final SqlManager sqlManager, final Table tableCurrent,
			final PrimaryKeyConstraint primaryKeyConstraint) throws SqlBaseException {
		if (primaryKeyConstraint != null) {
			PrimaryKeyConstraint primaryKeyConstraintCurrent = null;

			primaryKeyConstraintCurrent = tableCurrent.getPrimaryKey();
			if (primaryKeyConstraintCurrent != null) {
				if (!primaryKeyConstraint.equals(primaryKeyConstraintCurrent)) {
					alter(conn, sqlManager, tableCurrent, primaryKeyConstraint);
				}
			}
			else {
				alter(conn, sqlManager, tableCurrent, primaryKeyConstraint);
			}
		}
	}

	/**
	 * This method compares the current table with the desired table.
	 * 
	 * @param conn Connection used for all database transactions
	 * @param sqlManager SqlManager used for all database transactions
	 * @param tableCurrent Table definition of the current table.
	 * @param table Table definition of the desired table.
	 * @throws SqlBaseException
	 */
	private void compare(final Connection conn, final SqlManager sqlManager, final Table tableCurrent, final Table table)
			throws SqlBaseException {
		final List<Column> listChangedColumns = getChangedColumns(tableCurrent, table);
		if (!listChangedColumns.isEmpty()) {
			// drop all references to the column to avoid conflicts in the changing column--any existing keys will be
			// re-added when
			// comparing the keys
			final List<Constraint> listCurrentConstraints = tableCurrent.getConstraintList();

			// check foreign keys first due to the way MySQL makes sure there is an index on foreign keys
			for (int nConstraintIndex = listCurrentConstraints.size() - 1; nConstraintIndex >= 0; --nConstraintIndex) {
				final Constraint constraintCurrent = listCurrentConstraints.get(nConstraintIndex);
				if (constraintCurrent instanceof ForeignKeyConstraint) {
					if (dropContainingConstraint(conn, sqlManager, tableCurrent, listChangedColumns, constraintCurrent)) {
						listCurrentConstraints.remove(nConstraintIndex);
					}
				}
			}

			// check the rest of the keys
			for (int nConstraintIndex = listCurrentConstraints.size() - 1; nConstraintIndex >= 0; --nConstraintIndex) {
				final Constraint constraintCurrent = listCurrentConstraints.get(nConstraintIndex);
				// don't check primary keys (again, MySQL) or foreign keys
				if (!(constraintCurrent instanceof ForeignKeyConstraint) &&
						!(constraintCurrent instanceof PrimaryKeyConstraint)) {
					if (dropContainingConstraint(conn, sqlManager, tableCurrent, listChangedColumns, constraintCurrent)) {
						listCurrentConstraints.remove(nConstraintIndex);
					}
				}
			}

			// now go through and change the columns
			for (int nIndex = 0; nIndex < listChangedColumns.size(); ++nIndex) {
				alter(conn, sqlManager, tableCurrent, (ColumnDefinition)listChangedColumns.get(nIndex));
			}
		}

		compare(conn, sqlManager, tableCurrent, table.getPrimaryKey());
		compare(conn, sqlManager, tableCurrent, table.getLogicalKey());

		final Iterator<Constraint> iterConstraints = table.getConstraints();
		while (iterConstraints.hasNext()) {
			compare(conn, sqlManager, tableCurrent, iterConstraints.next());
		}
	}

	/**
	 * This method checks to see if the column is involved in the constraint.
	 * 
	 * @param constraint Constraint being verified.
	 * @param column Column being verified.
	 */
	private boolean constraintContainsColumn(final Constraint constraint, final Column column) {
		boolean bContained = false;

		if (constraint != null) {
			bContained = constraint.hasColumn(column);
		}

		return bContained;
	}

	/**
	 * This method drops the named constraint from the table.
	 * 
	 * @param conn Connection used for all database transactions
	 * @param sqlManager SqlManager used for all database transactions
	 * @param table Table definition for the currently defined table
	 * @param constraint Constraint definition of the constraint to drop.
	 * @throws SqlBaseException
	 */
	private void drop(final Connection conn, final SqlManager sqlManager, final Table table, final Constraint constraint)
			throws SqlBaseException {
		// foreign keys can't be updated, so drop and recreate it
		final SqlAlter sqlAlter = new SqlAlter(table);

		sqlAlter.drop(constraint);

		m_log.info("Changing database with: " + sqlAlter.toString());

		sqlManager.executeUpdate(sqlAlter, conn);

		// make sure the definition reflects the changes in the database
		table.drop(constraint);
	}

	/**
	 * This method drops the constraint on the table if it contains the specified column.
	 * 
	 * @param conn Connection used for all database transactions
	 * @param sqlManager SqlManager used for all database transactions
	 * @param tableCurrent Table definition for the currently defined table
	 * @param constraintCurrent Constraint containing the current definition of the constraint to check
	 * @param column Column from the current table of the column to check
	 * @throws SqlBaseException
	 */
	private boolean dropContainingConstraint(final Connection conn, final SqlManager sqlManager,
			final Table tableCurrent, final Constraint constraintCurrent, final Column column) throws SqlBaseException {
		boolean bDropped = false;
		if (constraintContainsColumn(constraintCurrent, column)) {
			drop(conn, sqlManager, tableCurrent, constraintCurrent);

			bDropped = true;
		}

		return bDropped;
	}

	/**
	 * This method checks the constraint against the list of columns and drops the constraint if any of the columns play
	 * a part.
	 * 
	 * @param conn Connection used for all database transactions
	 * @param sqlManager SqlManager used for all database transactions
	 * @param tableCurrent Table definition for the currently defined table
	 * @param listChangedColumns List containing the columns that have changed
	 * @param constraintCurrent Constraint containing the current definition of the constraint to check
	 * @throws SqlBaseException
	 */
	private boolean dropContainingConstraint(final Connection conn, final SqlManager sqlManager,
			final Table tableCurrent, final List<Column> listChangedColumns, final Constraint constraintCurrent)
			throws SqlBaseException {
		boolean bDropped = false;

		// check to see if any changing columns are in the key
		for (int nIndex = 0; nIndex < listChangedColumns.size() && !bDropped; ++nIndex) {
			final Column column = listChangedColumns.get(nIndex);
			final Column columnCurrent = tableCurrent.getColumn(column.getName());

			// only need to check if the column currently exists
			if (columnCurrent != null) {
				bDropped = dropContainingConstraint(conn, sqlManager, tableCurrent, constraintCurrent, columnCurrent);
			}
		}

		return bDropped;
	}

	/**
	 * This method returns the list of modified columns. This includes column that are new or those that have a new
	 * definition.
	 * 
	 * @param tableCurrent Table definition of the current table.
	 * @param table Table definition of the desired table.
	 */
	private List<Column> getChangedColumns(final Table tableCurrent, final Table table) {
		final List<Column> listChangedColumns = new ArrayList<Column>();

		// collect a list of new or modified columns
		final Iterator<ColumnDefinition> iterColumns = table.getColumns();
		while (iterColumns.hasNext()) {
			final Column column = iterColumns.next();
			final Column columnCurrent = tableCurrent.getColumn(column.getName());

			if (columnCurrent != null) {
				if (!column.equals(columnCurrent)) {
					listChangedColumns.add(column);
				}
			}
			else {
				listChangedColumns.add(column);
			}
		}

		return listChangedColumns;
	}
}

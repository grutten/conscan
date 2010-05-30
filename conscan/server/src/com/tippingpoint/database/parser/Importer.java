package com.tippingpoint.database.parser;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.DataConversion;
import com.tippingpoint.database.DatabaseException;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.Condition;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;
import com.tippingpoint.sql.Operation;
import com.tippingpoint.sql.ParameterizedValue;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.sql.SqlBuilderException;
import com.tippingpoint.sql.SqlExecutionException;
import com.tippingpoint.sql.SqlInsert;
import com.tippingpoint.sql.SqlManagerException;
import com.tippingpoint.sql.SqlQuery;
import com.tippingpoint.sql.SqlUpdate;
import com.tippingpoint.sql.ValueCondition;
import com.tippingpoint.sql.base.SqlExecution;

/**
 * This class is used for importing data into a schema.
 */
public final class Importer {
	private static Log m_log = LogFactory.getLog(Importer.class);

	/** This member holds the current table being imported into. */
	private Table m_activeTable;

	/** This member holds the list of messages from processing the imports. */
	private final List<String> m_listMessages = new ArrayList<String>();

	/** This member holds the current values being imported. */
	private final Map<Column, Object> m_mapValues = new HashMap<Column, Object>();

	/** This member holds the schema being imported into. */
	private final Schema m_schema;

	/**
	 * This method constructs a new importer class for the passed in schema.
	 * 
	 * @param schema Schema being imported into.
	 */
	public Importer(final Schema schema) {
		m_schema = schema;
	}

	/**
	 * This method clears the data from the currently imported row.
	 */
	public void clearRow() {
		m_mapValues.clear();
	}

	/**
	 * This method returns the active table in the importer.
	 */
	public Table getActiveTable() {
		return m_activeTable;
	}

	/**
	 * This method return the schema being used for the import process.
	 */
	public Schema getSchema() {
		return m_schema;
	}

	/**
	 * This method returns the value represented by the table value.
	 */
	public Object getValue(final TableValue tableValue) {
		Object objValue = null;

		final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
		final Table table = tableValue.getTable();
		final SqlQuery sqlQuery = new SqlQuery();

		sqlQuery.add(table);

		Column column = null;
		final Constraint constraint = table.getPrimaryKey();
		final Iterator<Column> iterColumns = constraint.getColumns();
		if (iterColumns != null && iterColumns.hasNext()) {
			while (iterColumns.hasNext()) {
				column = iterColumns.next();
				sqlQuery.add(column);
			}
		}

		final Iterator<ColumnValue> iterColumnValues = tableValue.getColumnValues();
		if (iterColumnValues != null && iterColumnValues.hasNext()) {
			while (iterColumnValues.hasNext()) {
				final ColumnValue columnValue = iterColumnValues.next();

				sqlQuery.add(new ValueCondition(columnValue.getColumn(), Operation.EQUALS, columnValue.getValue()));
			}
		}

		Connection conn = null;
		SqlExecution sqlExecution = null;
		ResultSet rs = null;

		try {
			conn = manager.getConnection();
			sqlExecution = manager.getSqlManager().getExecution(sqlQuery);
			rs = sqlExecution.executeQuery(conn);
			if (rs != null && rs.next()) {
				objValue = sqlExecution.getObject(column, rs);
			}
		}
		catch (final DatabaseException e) {
			addMessage("Database Exception getting value", e);
		}
		catch (final SqlManagerException e) {
			addMessage("SQL Builder Exception getting value", e);
		}
		catch (final SQLException e) {
			addMessage("SQL Exception getting value", e);
		}
		catch (final SqlBuilderException e) {
			addMessage("SQL Builder Exception getting value", e);
		}
		catch (final SqlExecutionException e) {
			addMessage("SQL Execution Exception getting value", e);
		}
		finally {
			ConnectionManager.close(conn, sqlExecution, rs);
		}

		return objValue;
	}

	/**
	 * This method returns if there is an active table.
	 */
	public boolean hasActiveTable() {
		return m_activeTable != null;
	}

	/**
	 * This method save the current row being imported.
	 */
	public void saveRow() {
		// only save a row if the table has been specified--also means that a valid table has been specified
		if (m_activeTable != null) {
			boolean bContinue = checkConstraintWithValues(m_activeTable.getPrimaryKey());

			if (bContinue) {
				bContinue = checkConstraintWithValues(m_activeTable.getLogicalKey());
				if (bContinue) {
					final Iterator<Constraint> iterConstraints = m_activeTable.getConstraints();
					if (iterConstraints != null && iterConstraints.hasNext()) {
						while (iterConstraints.hasNext() && bContinue) {
							final Constraint constraint = iterConstraints.next();
							if (constraint.isUnique()) {
								bContinue = checkConstraintWithValues(constraint);
							}
						}
					}
					// if we check all unique constraints and we still need to continue, then just insert the row
					if (bContinue) {
						final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
						final SqlInsert sqlInsert = new SqlInsert(m_activeTable);

						// only add the columns that we have values for
						final Iterator<Column> iterColumns = m_mapValues.keySet().iterator();
						if (iterColumns != null && iterColumns.hasNext()) {
							while (iterColumns.hasNext()) {
								final Column column = iterColumns.next();

								sqlInsert.add(new ParameterizedValue(column, m_mapValues.get(column)));
							}
						}

						Connection conn = null;

						try {
							conn = manager.getConnection();
							manager.getSqlManager().executeUpdate(sqlInsert, conn);

							m_log.debug("Inserting row into '" + m_activeTable + "'");
						}
						catch (final SqlManagerException e) {
							addMessage("SQL Builder Exception inserting row.", e);
						}
						catch (final SqlExecutionException e) {
							addMessage("SQL Execution Exception inserting row.", e);
						}
						catch (final SQLException e) {
							addMessage("SQL Exception inserting row.", e);
						}
						catch (final SqlBaseException e) {
							addMessage("SQL Base Exception inserting row.", e);
						}
						finally {
							ConnectionManager.close(conn, null, null);
						}
					}
				}
			}
		}
	}

	/**
	 * This method sets a column value.
	 * 
	 * @param column Column for which the value is being added.
	 * @param objValue Object containing the value.
	 */
	public void setColumnValue(final Column column, final Object objValue) {
		m_mapValues.put(column, objValue);
	}

	/**
	 * This method sets the current table in the schema based on name.
	 */
	public void setTable(final String strTableName) {
		m_activeTable = null;

		if (strTableName != null) {
			m_activeTable = m_schema.getTable(strTableName);
			if (m_activeTable == null) {
				addMessage("Data for table '" + strTableName +
						"' could not be imported since that table could not be found.");
			}
		}
	}

	/**
	 * This method adds the message to the message queue.
	 * 
	 * @param strMessage String containing the message.
	 */
	private void addMessage(final String strMessage) {
		addMessage(strMessage, null);
	}

	/**
	 * This method adds the message to the message queue.
	 * 
	 * @param strMessage String containing the message.
	 */
	private void addMessage(final String strMessage, final Throwable t) {
		m_listMessages.add(strMessage);

		if (t != null) {
			t.printStackTrace();
		}
	}

	/**
	 * This method checks to see if the a row can be found based on the constraint and updates that row if found.
	 * 
	 * @param constraint Constraint to be checked.
	 */
	private boolean checkConstraintWithValues(final Constraint constraint) {
		boolean bContinue = true;

		// if there is a constraint and values for the columns have been specified, then check to see if the row exists
		if (constraint != null && valuesSpecified(constraint)) {
			final List<Column> listExtraColumns = getExtraColumns(constraint);
			final List<Condition> listConditions = getConditions(constraint);

			final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
			final SqlQuery sqlQuery = new SqlQuery();

			// create a statement of the form:
			// SELECT <extra columns> FROM <table> WHERE <constraint columns are specified>
			sqlQuery.add(m_activeTable);

			// add the extra columns as select columns
			for (int nIndex = 0; nIndex < listExtraColumns.size(); ++nIndex) {
				sqlQuery.add(listExtraColumns.get(nIndex));
			}

			// add the conditions based on the constraint columns
			for (int nIndex = 0; nIndex < listConditions.size(); ++nIndex) {
				sqlQuery.add(listConditions.get(nIndex));
			}

			Connection conn = null;
			SqlExecution sqlExecution = null;
			ResultSet rs = null;

			try {
				conn = manager.getConnection();
				sqlExecution = manager.getSqlManager().getExecution(sqlQuery);
				rs = sqlExecution.executeQuery(conn);
				if (rs != null && rs.next()) {
					// found a row in the DB for this row in the data--check to see if all the values match
					boolean bMatch = true;
					for (int nIndex = 0; nIndex < listExtraColumns.size() && bMatch; ++nIndex) {
						final Column column = listExtraColumns.get(nIndex);
						final Object objNewValue = m_mapValues.get(column);
						final Object objExstingValue = sqlExecution.getObject(column, rs);

						if (objNewValue != null && objExstingValue != null &&
								objNewValue.getClass().equals(objExstingValue.getClass())) {
							bMatch = ObjectUtils.equals(objExstingValue, objNewValue);
						}
						else {
							final DataConversion conversion = manager.getSqlManager().getConverter();

							bMatch =
								ObjectUtils.equals(conversion.convertToSqlObject(column.getType(), objExstingValue),
										conversion.convertToSqlObject(column.getType(), objNewValue));
						}
					}

					if (!bMatch) {
						final SqlUpdate sqlUpdate = new SqlUpdate(m_activeTable);

						// add the extra columns as select columns
						for (int nIndex = 0; nIndex < listExtraColumns.size(); ++nIndex) {
							final Column column = listExtraColumns.get(nIndex);
							sqlUpdate.add(new ParameterizedValue(column, m_mapValues.get(column)));
						}

						// add the conditions based on the constraint columns
						for (int nIndex = 0; nIndex < listConditions.size(); ++nIndex) {
							sqlUpdate.add(listConditions.get(nIndex));
						}

						sqlExecution.close();
						sqlExecution = null;

						manager.getSqlManager().executeUpdate(sqlUpdate, conn);

						m_log.debug("Updating row in '" + m_activeTable + "'");
					}

					bContinue = false; // found the record and did what we could
				}
			}
			catch (final DatabaseException e) {
				addMessage("Database Exception checking on row.", e);
			}
			catch (final SqlManagerException e) {
				addMessage("SQL Builder Exception checking on row.", e);
			}
			catch (final SqlExecutionException e) {
				addMessage("SQL Execution Exception updating row.", e);
			}
			catch (final SQLException e) {
				addMessage("SQL Exception checking on row.", e);
			}
			catch (final SqlBuilderException e) {
				addMessage("SQL Builder Exception checking on row.", e);
			}
			catch (final SqlBaseException e) {
				addMessage("SQL Exception checking on row.", e);
			}
			finally {
				ConnectionManager.close(conn, sqlExecution, rs);
			}
		}

		return bContinue;
	}

	/**
	 * This method returns a collection of value conditions suitable for the query or the update, based on the
	 * constraint columns.
	 * 
	 * @param constraint Constraint to be checked.
	 */
	private List<Condition> getConditions(final Constraint constraint) {
		final List<Condition> listConditions = new ArrayList<Condition>();

		// add the conditions based on the constraint columns
		final Iterator<Column> iterColumns = constraint.getColumns();
		if (iterColumns != null && iterColumns.hasNext()) {
			while (iterColumns.hasNext()) {
				final Column column = iterColumns.next();

				listConditions.add(new ValueCondition(column, Operation.EQUALS, m_mapValues.get(column)));
			}
		}

		return listConditions;
	}

	/**
	 * This method returns a list of columns that have values specified, but is not in the constraint.
	 * 
	 * @param constraint Constraint to be checked.
	 */
	private List<Column> getExtraColumns(final Constraint constraint) {
		final List<Column> listExtraColumns = new ArrayList<Column>();

		// determine the columns with values, not in the constraint
		final Iterator<Column> iterExtraColumns = m_mapValues.keySet().iterator();
		if (iterExtraColumns != null && iterExtraColumns.hasNext()) {
			while (iterExtraColumns.hasNext()) {
				final Column column = iterExtraColumns.next();

				if (!constraint.hasColumn(column)) {
					listExtraColumns.add(column);
				}
			}
		}

		// if no extra columns, just use the constraint columns
		if (listExtraColumns.isEmpty()) {
			final Iterator<Column> iterConstraintColumns = constraint.getColumns();
			if (iterConstraintColumns != null && iterConstraintColumns.hasNext()) {
				while (iterConstraintColumns.hasNext()) {
					listExtraColumns.add(iterConstraintColumns.next());
				}
			}
		}

		return listExtraColumns;
	}

	/**
	 * This method checks to see if all the columns in the constraint have been specified.
	 * 
	 * @param constraint Constraint to be checked.
	 */
	private boolean valuesSpecified(final Constraint constraint) {
		boolean bValid = true;
		final Iterator<Column> iterColumns = constraint.getColumns();
		if (iterColumns != null && iterColumns.hasNext()) {
			while (iterColumns.hasNext() && bValid) {
				final Column column = iterColumns.next();

				bValid = m_mapValues.containsKey(column);
			}
		}

		return bValid;
	}
}

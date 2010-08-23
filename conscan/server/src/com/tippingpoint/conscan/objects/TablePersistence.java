package com.tippingpoint.conscan.objects;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;
import com.tippingpoint.sql.Operation;
import com.tippingpoint.sql.ParameterizedValue;
import com.tippingpoint.sql.SqlBuilderException;
import com.tippingpoint.sql.SqlExecutionException;
import com.tippingpoint.sql.SqlInsert;
import com.tippingpoint.sql.SqlManagerException;
import com.tippingpoint.sql.SqlUpdate;
import com.tippingpoint.sql.ValueCondition;
import com.tippingpoint.sql.base.SqlExecution;

/**
 * This class is used to persist content to a table.
 */
public class TablePersistence implements Persistence {
	/** This member holds the SQL used to generate an insert statement. */
	private SqlInsert m_insert;

	/** This member holds the list of names of elements for this object. */
	private final List<String> m_listFields;

	/** This member holds the id column for the table. */
	private Column m_PrimaryKeyColumn;

	/** This member holds the table associated with the object. */
	private final Table m_table;

	/** This member holds the SQL used to generate an update statement, base on primary key. */
	private SqlUpdate m_update;

	/**
	 * This method constructs a new table persistence instance for the given table.
	 */
	public TablePersistence(final Table table) {
		m_table = table;

		m_listFields = new ArrayList<String>();

		final Iterator<ColumnDefinition> iterColumns = m_table.getColumns();
		if (iterColumns != null) {
			while (iterColumns.hasNext()) {
				final Column column = iterColumns.next();
				m_listFields.add(column.getName());
			}
		}

		if (m_table.hasIdPrimaryKey()) {
			m_PrimaryKeyColumn = m_table.getPrimaryKeyColumn();
		}

		generateInsert();
		generateUpdate();
	}

	/**
	 * This method returns a list of all the named elements of the business object.
	 */
	@Override
	public Iterator<String> getFields() {
		return m_listFields.iterator();
	}

	/**
	 * This method returns the table which is being persisted.
	 */
	public Table getTable() {
		return m_table;
	}

	/**
	 * This method writes the data to the persistence layer.
	 * 
	 * @throws SqlExecutionException
	 * @throws SqlManagerException
	 * @throws SqlBuilderException
	 */
	public void insert(final Map<String, FieldValue> mapValues) throws SqlExecutionException, SqlManagerException,
			SqlBuilderException {
		final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();

		Connection conn = null;
		SqlExecution sqlInsert = null;

		try {
			conn = manager.getConnection();
			sqlInsert = manager.getSqlManager().getExecution(m_insert);

			// populate the insert statement with the parameters from the map
			setValues(sqlInsert, mapValues);

			// execute the insert
			sqlInsert.executeUpdate(conn);
		}
		catch (final SQLException e) {
			throw new SqlExecutionException("Error inserting into table.", e);
		}
		finally {
			ConnectionManager.close(conn, sqlInsert, null);
		}
	}

	/**
	 * This method saves the object, if necessary.
	 * 
	 * @param mapValues Map of values used to persist the object.
	 * @throws SqlBuilderException
	 * @throws SqlManagerException
	 * @throws SqlExecutionException
	 */
	public void save(final Map<String, FieldValue> mapValues) throws SqlExecutionException, SqlManagerException,
			SqlBuilderException {
		if (m_PrimaryKeyColumn != null) {
			final FieldValue value = mapValues.get(m_PrimaryKeyColumn.getName());
			if (value != null && value.getValue() != null) {
				update(mapValues);
			}
			else {
				insert(mapValues);
			}
		}
		else {
			insert(mapValues);
		}
	}

	/**
	 * This method writes the data to the persistence layer.
	 * 
	 * @throws SqlExecutionException
	 * @throws SqlManagerException
	 * @throws SqlBuilderException
	 */
	public void update(final Map<String, FieldValue> mapValues) throws SqlExecutionException, SqlManagerException,
			SqlBuilderException {
		final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();

		Connection conn = null;
		SqlExecution sqlUpdate = null;

		try {
			conn = manager.getConnection();
			sqlUpdate = manager.getSqlManager().getExecution(m_update);

			// populate the update statement with the parameters from the map
			setValues(sqlUpdate, mapValues);

			// execute the update
			sqlUpdate.executeUpdate(conn);
		}
		catch (final SQLException e) {
			throw new SqlExecutionException("Error updating into table.", e);
		}
		finally {
			ConnectionManager.close(conn, sqlUpdate, null);
		}
	}

	/**
	 * This method generates the statement for the insert into this table.
	 */
	private void generateInsert() {
		final Iterator<ColumnDefinition> iterColumns = m_table.getColumns();
		if (iterColumns != null && iterColumns.hasNext()) {
			final SqlInsert sqlInsert = new SqlInsert(m_table);

			// loop through all the columns to put into the statement
			while (iterColumns.hasNext()) {
				final ColumnDefinition column = iterColumns.next();

				// add all columns, but the primary key
				if (m_PrimaryKeyColumn == null || !m_PrimaryKeyColumn.equals(column)) {
					sqlInsert.add(new ParameterizedValue(column, null));
				}
			}

			m_insert = sqlInsert;
		}
	}

	/**
	 * This method generates the statement for the update of this table.
	 */
	private void generateUpdate() {
		final Iterator<ColumnDefinition> iterColumns = m_table.getColumns();
		if (iterColumns != null && iterColumns.hasNext()) {
			final SqlUpdate sqlUpdate = new SqlUpdate(m_table);

			// loop through all the columns to put into the statement
			while (iterColumns.hasNext()) {
				final ColumnDefinition column = iterColumns.next();

				// add all columns, but the primary key
				if (m_PrimaryKeyColumn == null || !m_PrimaryKeyColumn.equals(column)) {
					sqlUpdate.add(new ParameterizedValue(column, null));
				}
			}

			if (m_PrimaryKeyColumn != null) {
				sqlUpdate.add(new ValueCondition(m_PrimaryKeyColumn, Operation.EQUALS, null));
			}

			m_update = sqlUpdate;
		}
	}

	/**
	 * This method sets the values in the passed in command.
	 */
	private void setValues(final SqlExecution sqlExecution, final Map<String, FieldValue> mapValues) {
		final Iterator<ParameterizedValue> iterParameters = sqlExecution.getParameters();
		if (iterParameters != null && iterParameters.hasNext()) {
			while (iterParameters.hasNext()) {
				final ParameterizedValue parameterizedValue = iterParameters.next();
				final Column column = parameterizedValue.getColumn();
				final FieldValue fieldValue = mapValues.get(column.getName());

				parameterizedValue.setValue(fieldValue.getValue());
			}
		}
	}

	/**
	 * This method adds business object builders for each table in the passed in schema.
	 */
	public static void registerTables(final Schema schema) {
		final Iterator<Table> iterTables = schema.getTables();
		if (iterTables != null && iterTables.hasNext()) {
			final BusinessObjectBuilderFactory businessObjectBuilderFactory = BusinessObjectBuilderFactory.get();
			while (iterTables.hasNext()) {
				final Table table = iterTables.next();

				businessObjectBuilderFactory.register(new TableBusinessObjectBuilder(table));
			}
		}
	}
}

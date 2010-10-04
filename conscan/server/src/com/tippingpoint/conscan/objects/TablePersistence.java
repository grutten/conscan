package com.tippingpoint.conscan.objects;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.ForeignKey;
import com.tippingpoint.database.ForeignKeyConstraint;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
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
 * This class is used to persist content to a table.
 */
public class TablePersistence implements Persistence {
	/** This member holds the list of names of elements for this object. */
	private final List<String> m_listFields;

	/** This member holds the rules applied to the data. */
	private final List<DataRule> m_listRules;

	/** This member holds the id column for the table. */
	private Column m_PrimaryKeyColumn;

	/** This member holds the SQL used to generate an insert statement. */
	private SqlInsert m_sqlInsert;

	/** This member holds the SQL used to read an object based on primary key. */
	private SqlQuery m_sqlQueryById;

	/** This member holds the SQL used to generate an update statement, based on primary key. */
	private SqlUpdate m_sqlUpdate;

	/** This member holds the table associated with the object. */
	private final Table m_table;

	/**
	 * This method constructs a new table persistence instance for the given table.
	 */
	public TablePersistence(final Table table) {
		m_table = table;

		m_listFields = new ArrayList<String>();
		m_listRules = new ArrayList<DataRule>();

		final Iterator<ColumnDefinition> iterColumns = m_table.getColumns();
		if (iterColumns != null) {
			while (iterColumns.hasNext()) {
				final Column column = iterColumns.next();
				m_listFields.add(column.getName());

				if ("created".equalsIgnoreCase(column.getName())) {
					m_listRules.add(new NowDataRule(column.getName()));
				}
				else if ("modified".equalsIgnoreCase(column.getName())) {
					m_listRules.add(new NowDataRule(column.getName()));
				}
			}
		}

		if (m_table.hasIdPrimaryKey()) {
			m_PrimaryKeyColumn = m_table.getPrimaryKeyColumn();
		}

		generateInsert();
		generateUpdate();
		generateQueryById();
	}

	/**
	 * This method returns the data from the persistence layer for the given identifier.
	 * 
	 * @throws SqlBaseException
	 */
	public Map<String, FieldValue> get(final Object objId) throws SqlBaseException {
		final Map<String, FieldValue> mapValues = new LinkedHashMap<String, FieldValue>();

		mapValues.put(m_PrimaryKeyColumn.getName(), new FieldValue(m_PrimaryKeyColumn.getName(), objId));

		final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();

		Connection conn = null;
		SqlExecution sqlQueryById = null;
		ResultSet rs = null;

		try {
			conn = manager.getConnection();
			sqlQueryById = manager.getSqlManager().getExecution(m_sqlQueryById);

			// populate the insert statement with the parameters from the map
			setValues(sqlQueryById, mapValues);

			// execute the insert
			rs = sqlQueryById.executeQuery(conn);
			if (rs.next()) {
				final Iterator<Column> iterColumns = sqlQueryById.getColumnMap();
				if (iterColumns != null && iterColumns.hasNext()) {
					int nIndex = 1;
					while (iterColumns.hasNext()) {
						final Column column = iterColumns.next();

						mapValues.put(column.getName(), new FieldValue(column.getName(), column.getType().getResult(rs,
								nIndex++)));
					}
				}
			}
		}
		catch (final SQLException e) {
			throw new SqlExecutionException("Error reading from table.", e);
		}
		finally {
			ConnectionManager.close(conn, sqlQueryById, rs);
		}

		return mapValues;
	}

	/**
	 * This method returns a collection of objects representing all of the objects of this type.
	 * 
	 * @param listCommonValues List containing the values that will be common to all the objects..
	 * @throws SqlBaseException
	 */
	public List<Map<String, FieldValue>> getAll(final List<FieldValue> listCommonValues) throws SqlBaseException {
		final List<Map<String, FieldValue>> listValues = new ArrayList<Map<String, FieldValue>>();

		final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();

		Connection conn = null;
		SqlExecution sql = null;
		ResultSet rs = null;

		final SqlQuery sqlQuery = getQuery(listCommonValues);

		try {
			conn = manager.getConnection();
			sql = manager.getSqlManager().getExecution(sqlQuery);

			// execute the query
			rs = sql.executeQuery(conn);
			while (rs.next()) {
				final Iterator<Column> iterColumns = sql.getColumnMap();
				if (iterColumns != null && iterColumns.hasNext()) {
					final Map<String, FieldValue> mapValues = new LinkedHashMap<String, FieldValue>();

					listValues.add(mapValues);

					int nIndex = 1;
					while (iterColumns.hasNext()) {
						final Column column = iterColumns.next();

						mapValues.put(column.getName(), new FieldValue(column.getName(), column.getType().getResult(rs,
								nIndex++)));
					}
				}
			}
		}
		catch (final SQLException e) {
			throw new SqlExecutionException("Error reading from table.", e);
		}
		finally {
			ConnectionManager.close(conn, sql, rs);
		}

		return listValues;
	}

	/**
	 * This method returns a list of all the named elements of the business object.
	 */
	@Override
	public Iterator<String> getFields() {
		return m_listFields.iterator();
	}

	/**
	 * This method returns the name of the identifier field, if available.
	 */
	public String getIdentifierName() {
		return m_PrimaryKeyColumn != null ? m_PrimaryKeyColumn.getName() : null;
	}

	/**
	 * This method returns a list of business object names that are related to this object.
	 */
	public List<String> getRelatedNames() {
		List<String> listRelatedNames = null;

		final List<ForeignKeyConstraint> listReferences = m_table.getReferences();
		if (listReferences != null && !listReferences.isEmpty()) {
			listRelatedNames = new ArrayList<String>();
			for (final ForeignKeyConstraint foreignKeyConstraint : listReferences) {
				final Table table = foreignKeyConstraint.getTable();
				listRelatedNames.add(table.getName());
			}
		}

		return listRelatedNames;
	}

	/**
	 * This method returns a list containing the named related objects.
	 * 
	 * @param strRelatedName String containing the name of the related object
	 * @param mapValues Map of values used to persist the object.
	 * @throws SqlBaseException
	 */
	public List<BusinessObject> getReleatedObjects(final String strRelatedName, final Map<String, FieldValue> mapValues)
			throws SqlBaseException {
		List<BusinessObject> listReleatedObjects = null;

		final ForeignKeyConstraint foreignKeyConstraint = getRestraint(strRelatedName);
		if (foreignKeyConstraint != null) {
			listReleatedObjects = new ArrayList<BusinessObject>();

			final List<FieldValue> listValues = new ArrayList<FieldValue>();

			final Iterator<Column> iterColumns = foreignKeyConstraint.getColumns();
			if (iterColumns != null && iterColumns.hasNext()) {
				while (iterColumns.hasNext()) {
					final ForeignKey foreignKey = (ForeignKey)iterColumns.next();

					final Column columnParent = foreignKey.getParentColumn();
					final Column columnChild = foreignKey.getChildColumn();

					// get the value from the parent object
					final FieldValue value = mapValues.get(columnParent.getName());

					// add the condition based on the child's name
					listValues.add(new FieldValue(columnChild.getName(), value.getValue()));
				}
			}

			final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(strRelatedName);
			listReleatedObjects = builder.getAll(listValues);
		}

		return listReleatedObjects;
	}

	/**
	 * This method returns the table which is being persisted.
	 */
	public Table getTable() {
		return m_table;
	}

	/**
	 * This method returns the type of business object.
	 */
	public String getType() {
		return m_table.getName();
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
			sqlInsert = manager.getSqlManager().getExecution(m_sqlInsert);

			// apply any rules to the values
			applyRules(mapValues);

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
			sqlUpdate = manager.getSqlManager().getExecution(m_sqlUpdate);

			// apply any rules to the values
			applyRules(mapValues);

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
	 * This method applies any rules the the data before persisting the object.
	 */
	private void applyRules(final Map<String, FieldValue> mapValues) {
		// fire any rules to set data
		if (m_listRules != null && !m_listRules.isEmpty()) {
			for (final DataRule dataRule : m_listRules) {
				dataRule.apply(mapValues);
			}
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

			m_sqlInsert = sqlInsert;
		}
	}

	/**
	 * This method generates the statement to read an object of this table by primary key.
	 */
	private void generateQueryById() {
		if (m_PrimaryKeyColumn != null) {
			final SqlQuery sqlQuery = new SqlQuery();

			sqlQuery.add(m_table, true);

			sqlQuery.add(new ValueCondition(m_PrimaryKeyColumn, Operation.EQUALS, null));

			m_sqlQueryById = sqlQuery;
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

			m_sqlUpdate = sqlUpdate;
		}
	}

	/**
	 * This method generates the a query with conditions added for the common values.
	 * 
	 * @param listCommonValues List containing the values that will be common to all the objects..
	 */
	private SqlQuery getQuery(final List<FieldValue> listCommonValues) {
		final SqlQuery sqlQuery = new SqlQuery();

		sqlQuery.add(m_table, true);

		if (listCommonValues != null && !listCommonValues.isEmpty()) {
			for (final FieldValue fieldValue : listCommonValues) {
				final Column column = m_table.getColumn(fieldValue.getName());
				if (column != null) {
					sqlQuery.add(new ValueCondition(column, Operation.EQUALS, fieldValue.getValue()));
				}
			}
		}

		return sqlQuery;
	}

	/**
	 * This method returns the first foreign key referencing the named table.
	 * 
	 * @param strTableName String containing the foreign key table name.
	 */
	private ForeignKeyConstraint getRestraint(final String strRelatedName) {
		ForeignKeyConstraint foundForeignKeyConstraint = null;

		final List<ForeignKeyConstraint> listReferences = m_table.getReferences();
		if (listReferences != null && !listReferences.isEmpty()) {
			for (final ForeignKeyConstraint foreignKeyConstraint : listReferences) {
				final Table table = foreignKeyConstraint.getTable();
				if (strRelatedName.equals(table.getName())) {
					foundForeignKeyConstraint = foreignKeyConstraint;
					break;
				}
			}
		}

		return foundForeignKeyConstraint;
	}

	/**
	 * This method sets the values in the passed in command.
	 */
	private void setValues(final SqlExecution sqlExecution, final Map<String, FieldValue> mapValues) {
		// set the values into SQL statement
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

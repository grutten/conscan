package com.tippingpoint.sql.base;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.ColumnType;
import com.tippingpoint.database.ColumnTypeBoolean;
import com.tippingpoint.database.ColumnTypeDate;
import com.tippingpoint.database.ColumnTypeFactory;
import com.tippingpoint.database.ColumnTypeId;
import com.tippingpoint.database.ColumnTypeIdReference;
import com.tippingpoint.database.ColumnTypeInteger;
import com.tippingpoint.database.ColumnTypeSmallInteger;
import com.tippingpoint.database.ColumnTypeString;
import com.tippingpoint.database.ColumnTypeText;
import com.tippingpoint.database.DataConversion;
import com.tippingpoint.database.DatabaseException;
import com.tippingpoint.sql.Command;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.SqlAlter;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.sql.SqlBuilderException;
import com.tippingpoint.sql.SqlCreate;
import com.tippingpoint.sql.SqlDrop;
import com.tippingpoint.sql.SqlExecutionException;
import com.tippingpoint.sql.SqlInsert;
import com.tippingpoint.sql.SqlManagerException;
import com.tippingpoint.sql.SqlQuery;
import com.tippingpoint.sql.SqlUpdate;

public abstract class SqlManager {
	public static final String BOOLEAN_CHECK_PREFIX = "CK_BOOL_";
	protected static final String KEYWORD_MODIFY_COLUMN = "modify.column";
	protected static final String KEYWORD_MODIFY_CONSTRAINT = "modify.constraint";
	static final String KEYWORD_COLUMN_DEFAULT = "column.default";

	/** This member holds a conversion mechanism used to convert a value. */
	protected DataConversion m_converter = new DataConversion();

	/** This member holds the weak reference to the associated connection manager. */
	private WeakReference<ConnectionManager> m_connectionManager;

	/** This member holds the mapping of database column types to column types. */
	private final Map<String, String> m_mapColumnTypes = new HashMap<String, String>();

	/** This member holds a map of keywords. */
	private final Map<String, String> m_mapKeywords = new HashMap<String, String>();

	/** This member holds the factories used to execution SQL commands. */
	private final Map<Class<? extends Command>, SqlExecutionFactory> m_mapSqlExecutionFactories =
		new HashMap<Class<? extends Command>, SqlExecutionFactory>();

	/** This member holds the conversion of the types. */
	private final Map<Class<? extends ColumnType>, ColumnTypeConverter> m_mapTypeConverters =
		new HashMap<Class<? extends ColumnType>, ColumnTypeConverter>();

	/** This member holds the schema for the manager. */
	private SqlSchema m_sqlSchema;

	/**
	 * This method constructs a new builder, registering the types handled by the base class.
	 */
	public SqlManager() {
		register(new StaticColumnTypeConverter(ColumnTypeText.class, "TEXT"));
		register(new StaticColumnTypeConverter(ColumnTypeSmallInteger.class, "SMALLINT"));
		register(new StaticColumnTypeConverter(ColumnTypeInteger.class, "INTEGER"));
		register(new StringColumnTypeConverter(ColumnTypeString.class));
		register(new StaticColumnTypeConverter(ColumnTypeIdReference.class, "INTEGER"));
		register(new StaticColumnTypeConverter(ColumnTypeDate.class, "DATETIME"));
		register(new BooleanColumnTypeConverter());

		register(KEYWORD_MODIFY_COLUMN, "MODIFY");
		register(KEYWORD_MODIFY_CONSTRAINT, "MODIFY");
		register(KEYWORD_COLUMN_DEFAULT, "DEFAULT");

		register(new SqlCreateExecutionFactory(), SqlCreate.class);
		register(new SqlDropExecutionFactory(), SqlDrop.class);
		register(new SqlAlterExecutionFactory(), SqlAlter.class);
		register(new SqlQueryExecutionFactory(), SqlQuery.class);
		register(new SqlInsertExecutionFactory(), SqlInsert.class);
		register(new SqlUpdateExecutionFactory(), SqlUpdate.class);

		registerType("varchar", ColumnTypeString.TYPE);
		registerType("nvarchar", ColumnTypeString.TYPE);
		registerType("datetime", ColumnTypeDate.TYPE);
		registerType("int", ColumnTypeInteger.TYPE);
		registerType("smallint", ColumnTypeSmallInteger.TYPE);
		registerType("text", ColumnTypeText.TYPE);
		registerType("tinyint", ColumnTypeBoolean.TYPE);

		// set a default SQL schema
		setSqlSchema(new SqlSchema(this));
	}

	/**
	 * This method executes the command.
	 * 
	 * @throws SqlManagerException
	 * @throws SqlExecutionException
	 * @throws SqlBuilderException
	 * @throws DatabaseException
	 * @throws SqlBaseException
	 */
	@SuppressWarnings("null")
	public void execute(final Command sqlCommand, final Connection conn, final SqlResultAction action)
			throws SqlManagerException, SqlBuilderException, SqlExecutionException, DatabaseException {
		SqlExecution sqlExecution = null;
		ResultSet rs = null;

		try {
			sqlExecution = getExecution(sqlCommand);

			rs = sqlExecution.executeQuery(conn);
			if (rs != null) {
				action.beforeRows(sqlExecution);
				while (rs.next()) {
					action.process(sqlExecution, rs);
				}
				action.afterRows(sqlExecution);
			}
		}
		catch (final SQLException e) {
			throw new SqlExecutionException(sqlExecution.getSql(), e);
		}
		catch (final IOException e) {
			throw new SqlExecutionException(sqlExecution.getSql(), e);
		}
		finally {
			ConnectionManager.close(null, sqlExecution, null);
		}
	}

	/**
	 * This method executes the command.
	 * 
	 * @throws SQLException
	 * @throws SqlBaseException
	 * @throws SQLException
	 * @throws SqlExecutionException
	 * @throws SqlBuilderException
	 * @throws SqlManagerException
	 * @throws DatabaseException
	 */
	public void execute(final Command sqlCommand, final SqlResultAction action) throws SQLException,
			SqlManagerException, SqlBuilderException, SqlExecutionException, DatabaseException {
		Connection conn = null;

		try {
			conn = getConnectionManager().getConnection();

			execute(sqlCommand, conn, action);
		}
		finally {
			ConnectionManager.close(conn, null, null);
		}
	}

	/**
	 * This method executes the command.
	 * 
	 * @throws SqlBaseException
	 * @throws SQLException
	 */
	public int executeUpdate(final Command sqlCommand) throws SqlBaseException, SQLException {
		int nRowsUpdated = 0;

		Connection conn = null;

		try {
			conn = getConnectionManager().getConnection();

			nRowsUpdated = executeUpdate(sqlCommand, conn);
		}
		finally {
			ConnectionManager.close(conn, null, null);
		}

		return nRowsUpdated;
	}

	/**
	 * This method executes the command.
	 * 
	 * @throws SqlBaseException
	 */
	public int executeUpdate(final Command sqlCommand, final Connection conn) throws SqlBaseException {
		int nRowsUpdated = 0;
		SqlExecution sqlExecution = null;

		try {
			sqlExecution = getExecution(sqlCommand);
			nRowsUpdated = sqlExecution.executeUpdate(conn);
		}
		finally {
			ConnectionManager.close(null, sqlExecution, null);
		}

		return nRowsUpdated;
	}

	/**
	 * This method returns the connection manager associated with this SQL manager.
	 */
	public ConnectionManager getConnectionManager() {
		ConnectionManager manager = null;

		if (m_connectionManager != null) {
			manager = m_connectionManager.get();
		}

		if (manager == null) {
			throw new IllegalStateException("Returning a null connection manager.");
		}

		return manager;
	}

	/**
	 * This method returns the converter set for this builder.
	 */
	public DataConversion getConverter() {
		return m_converter;
	}

	/**
	 * This method is used to generate an execution instance for the given SQL statement.
	 * 
	 * @param sqlCreate
	 * @throws SqlManagerException
	 */
	public SqlExecution getExecution(final Command sqlCommand) throws SqlManagerException {
		SqlExecution execution = null;

		final SqlExecutionFactory factory = m_mapSqlExecutionFactories.get(sqlCommand.getClass());
		if (factory != null) {
			execution = factory.getExecution(this, sqlCommand);
		}

		if (execution == null) {
			throw new SqlManagerException("No factory found for SQL type of '" + sqlCommand.getClass() + "'");
		}

		return execution;
	}

	/**
	 * This method returns a key for the database.
	 * 
	 * @param strKeyword String containing a reference to a keyword.
	 */
	public String getKeyword(final String strKeyword) {
		return m_mapKeywords.get(strKeyword);
	}

	/**
	 * This method returns the schema for this manager. The schema is used to read the schema from the database.
	 */
	public SqlSchema getSqlSchema() {
		return m_sqlSchema;
	}

	/**
	 * This method returns the SQL used to query the database for the definitions of columns of the named table. The
	 * columns returned should be:
	 * <ul>
	 * <li>COLUMN_NAME - String containing the name of the column</li>
	 * <li>COLUMN_DEFAULT - Default value of the column</li>
	 * <li>IS_NULLABLE - String containing a 'YES' or 'NO' indicating if the column is nullable</li>
	 * <li>ID_COLUMN - int indicating if the column is an identity column</li>
	 * <li>DATA_TYPE - String containing the type of column</li>
	 * <li>CHARACTER_MAXIMUM_LENGTH - int containing the length of the text fields</li>
	 * </ul>
	 * 
	 * @param strDatabaseName String containing the name of the database.
	 * @param strTableName String containing the name of the table.
	 */
	public abstract String getTableDefinitionSql(String strDatabaseName, String strTableName);

	/**
	 * This method returns the database specific type for the column.
	 * 
	 * @param column Column instance for which to create the type.
	 * @return
	 */
	public String getType(final ColumnDefinition column) {
		String strType = null;
		final ColumnType type = column.getType();
		Class<?> clsType = type.getClass();
		ColumnTypeConverter converter = m_mapTypeConverters.get(clsType);

		// search up the hierarchy until a converter is found or the base class is found
		while (converter == null && !clsType.equals(ColumnType.class)) {
			// look at the super type
			clsType = clsType.getSuperclass();

			// see if there is a converter for the current column's type
			converter = m_mapTypeConverters.get(clsType);
		}

		if (converter != null) {
			strType = converter.get(column);
		}

		return strType;
	}

	/**
	 * This method translates the JDBC type to a column type.
	 * 
	 * @param nJdbcType int containing the JDBC type
	 * @param strTypeName String containing the string version of the JDBC type
	 */
	public ColumnType getType(final int nJdbcType, final String strTypeName) {
		ColumnType type = null;

		if (Types.VARCHAR == nJdbcType) {
			type = ColumnTypeFactory.getFactory().get(ColumnTypeString.TYPE);
		}
		else if (Types.TIMESTAMP == nJdbcType) {
			type = ColumnTypeFactory.getFactory().get(ColumnTypeDate.TYPE);
		}
		else if (Types.INTEGER == nJdbcType) {
			type = ColumnTypeFactory.getFactory().get(ColumnTypeInteger.TYPE);
		}
		else if (Types.SMALLINT == nJdbcType) {
			type = ColumnTypeFactory.getFactory().get(ColumnTypeSmallInteger.TYPE);
		}
		else if (Types.CLOB == nJdbcType || Types.BLOB == nJdbcType || Types.LONGVARCHAR == nJdbcType) {
			type = ColumnTypeFactory.getFactory().get(ColumnTypeText.TYPE);
		}
		else {
			throw new RuntimeException("SqlBuilderImpl: Unknown JDBC type: " + nJdbcType);
		}

		return type;
	}

	/**
	 * This method translates the string type, as found in the SQL from getTableDefinitionSql(), to a column type.
	 * 
	 * @param strDataType String containing the data type.
	 * @param idColumn boolean indicating if the column is a ID column
	 */
	public ColumnType getType(final String strDataType, final boolean idColumn) {
		ColumnType type = null;

		// the values appear to be pretty consistent across databases (see INFORMATION_SCHEMA)
		if (idColumn) {
			type = ColumnTypeFactory.getFactory().get(ColumnTypeId.TYPE);
		}
		else {
			final String strType = m_mapColumnTypes.get(strDataType);
			if (strType != null) {
				type = ColumnTypeFactory.getFactory().get(strType);
			}
			else {
				type = ColumnTypeFactory.getFactory().get(strDataType);
			}
		}

		return type;
	}

	/**
	 * This method sets the connection manager associated with this SQL manager.
	 * 
	 * @param connectionManager ConnectionManager instance associated with this SQL manager.
	 */
	public void setConnectionManager(final ConnectionManager connectionManager) {
		m_connectionManager = new WeakReference<ConnectionManager>(connectionManager);
	}

	/**
	 * This method sets the SQL schema.
	 */
	public void setSqlSchema(final SqlSchema sqlSchema) {
		m_sqlSchema = sqlSchema;
	}

	/**
	 * This method registers a type conversion.
	 */
	protected void register(final ColumnTypeConverter converter) {
		final Class<? extends ColumnType> clsType = converter.getClassType();

		m_mapTypeConverters.put(clsType, converter);
	}

	/**
	 * This method registers a SQL Execution factory for this manager.
	 * 
	 * @param sqlExecutionCreateFactory SqlExecutionFactory used to generate the execution classes.
	 * @param clsCommand Class of command for which the factory is registered.
	 */
	protected void register(final SqlExecutionFactory sqlExecutionFactory, final Class<? extends Command> clsCommand) {
		m_mapSqlExecutionFactories.put(clsCommand, sqlExecutionFactory);
	}

	/**
	 * This member registers a keyword.
	 */
	protected void register(final String strKeyword, final String strValue) {
		m_mapKeywords.put(strKeyword, strValue);
	}

	/**
	 * This method registers a database type.
	 */
	protected void registerType(final String strDatabaseType, final String strColumnType) {
		m_mapColumnTypes.put(strDatabaseType, strColumnType);
	}

	/**
	 * This class is used to process result set lines.
	 */
	public static abstract class SqlResultAction {
		/**
		 * This method is called after the last row is processed. This method is only called if there are rows to be
		 * processed.
		 * 
		 * @param sqlExecution
		 * @throws IOException
		 */
		public void afterRows(final SqlExecution sqlExecution) throws IOException {
			// default actions is to do nothing
		}

		/**
		 * This method is called prior to the first row being processed. This method is only called if there are rows to
		 * be processed.
		 * 
		 * @param sqlExecution
		 * @throws IOException
		 */
		public void beforeRows(final SqlExecution sqlExecution) throws IOException {
			// default actions is to do nothing
		}

		/**
		 * This method is called for each row returned in the result set.
		 * 
		 * @param sqlExecution SqlExecution instance used to execute the query.
		 * @param rs ResultSet being processed.
		 * @throws IOException
		 * @throws DatabaseException
		 * @throws SQLException
		 */
		public abstract void process(SqlExecution sqlExecution, ResultSet rs) throws IOException, SQLException,
				DatabaseException;
	}

	/**
	 * This class returns the string version of the type for booleans.
	 */
	protected static class BooleanColumnTypeConverter extends ColumnTypeConverter {
		/**
		 * This method creates a new type converter.
		 */
		public BooleanColumnTypeConverter() {
			super(ColumnTypeBoolean.class);
		}

		/**
		 * This method returns the string version of the type.
		 */
		@Override
		public String get(final ColumnDefinition column) {
			final StringBuilder strBuffer = new StringBuilder();

			strBuffer.append("tinyint CONSTRAINT ");
			strBuffer.append(BOOLEAN_CHECK_PREFIX);
			strBuffer.append(column.getTable().getName().toUpperCase());
			strBuffer.append("_");
			strBuffer.append(column.getName().toUpperCase());
			strBuffer.append(" CHECK (");
			strBuffer.append(column.getName());
			strBuffer.append(" = 1 OR ");
			strBuffer.append(column.getName());
			strBuffer.append(" = 0)");

			return strBuffer.toString();
		}
	}

	/**
	 * This class returns the string version of the type.
	 */
	protected abstract static class ColumnTypeConverter {
		/** This member holds the class that is being converted. */
		private final Class<? extends ColumnType> m_clsType;

		/**
		 * This method creates a new type converter.
		 */
		public ColumnTypeConverter(final Class<? extends ColumnType> clsType) {
			m_clsType = clsType;
		}

		/**
		 * This method returns the string version of the type.
		 */
		public abstract String get(ColumnDefinition column);

		/**
		 * This method returns the class that is being converted.
		 */
		public Class<? extends ColumnType> getClassType() {
			return m_clsType;
		}
	}

	/**
	 * This class returns the string version of the type.
	 */
	protected static class StaticColumnTypeConverter extends ColumnTypeConverter {
		/** This member holds the string related to the type. */
		private final String m_strType;

		/**
		 * This method creates a new type converter.
		 */
		public StaticColumnTypeConverter(final Class<? extends ColumnType> clsType, final String strType) {
			super(clsType);

			m_strType = strType;
		}

		/**
		 * This method returns the string version of the type.
		 */
		@Override
		public String get(final ColumnDefinition column) {
			return m_strType;
		}
	}

	/**
	 * This class returns the string version of the type for variable strings
	 */
	protected static class StringColumnTypeConverter extends ColumnTypeConverter {
		/**
		 * This method creates a new type converter.
		 */
		public StringColumnTypeConverter(final Class<? extends ColumnType> clsType) {
			super(clsType);
		}

		/**
		 * This method returns the string version of the type.
		 */
		@Override
		public String get(final ColumnDefinition column) {
			return new StringBuilder().append("VARCHAR(").append(column.getLength()).append(')').toString();
		}
	}
}

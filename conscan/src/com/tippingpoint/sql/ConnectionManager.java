package com.tippingpoint.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Iterator;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.ColumnTypeFactory;
import com.tippingpoint.database.ColumnTypeId;
import com.tippingpoint.database.ColumnTypeIdReference;
import com.tippingpoint.database.ColumnTypeInteger;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.DatabaseElementException;
import com.tippingpoint.database.DatabaseException;
import com.tippingpoint.database.ForeignKey;
import com.tippingpoint.database.ForeignKeyConstraint;
import com.tippingpoint.database.Index;
import com.tippingpoint.database.PrimaryKeyConstraint;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.utilities.StringProperties;

/**
 * This class manages returning a connection.
 */
public final class ConnectionManager {
	private static final Log m_log = LogFactory.getLog(ConnectionManager.class);

	/** This member holds the source of the connections. */
	private ConnectionSource m_connections = new ConnectionSource();

	/** This member holds the schema definition of the tables found in the database associated with this manager. */
	private Schema m_schema;

	/** This member holds the SQL builder used to generate SQL statements against the database. */
	private SqlBuilder m_sqlBuilder;

	/**
	 * This method constructs an new default connection manager.
	 * 
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	public ConnectionManager(ConnectionSource connections) throws SQLException, DatabaseException {
		// make sure connection source is specified so a known error will occur
		if (connections == null) {
			connections = new ConnectionSource();
		}

		m_connections = connections;

		setSqlBuilder();
	}

	/**
	 * This method returns a connection from the datasource.
	 * 
	 * @return Valid Connection object.
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		return m_connections.getConnection();
	}

	/**
	 * This method returns the schema associated with the connections associated with this manager.
	 */
	public Schema getSchema() {
		return m_schema;
	}

	/**
	 * This method will return a schema based on the details found in the database.
	 * 
	 * @param dataConversion
	 * @throws SQLException
	 * @throws DatabaseElementException
	 */
	public Schema getSchema(final String strSchemaName) throws SQLException, DatabaseElementException {
		Schema schema = null;

		Connection conn = null;
		try {
			conn = getConnection();

			final DatabaseMetaData metaData = conn.getMetaData();

			schema = new Schema(strSchemaName);

			readSchema(metaData, schema);
		}
		finally {
			DbUtils.closeQuietly(conn);
		}

		return schema;
	}

	/**
	 * This method returns a SQL builder class specific to the database defined by the connections.
	 */
	public SqlBuilder getSqlBuilder() {
		return m_sqlBuilder;
	}

	/**
	 * This method sets the schema associated with the connections associated with this manager.
	 */
	public void setSchema(final Schema schema) {
		m_schema = schema;
	}

	/**
	 * This method reads in the information about the schema from the database.
	 * 
	 * @throws SQLException
	 * @throws DatabaseElementException
	 */
	private void readSchema(final DatabaseMetaData metaData, final Schema schema) throws SQLException,
			DatabaseElementException {
		ResultSet rs = null;

		try {
			rs = metaData.getTables(schema.getName(), null, null, new String[] {"TABLE"});

			while (rs.next()) {
				// final String strTableCatalog = rs.getString("TABLE_CAT");
				// final String strTableSchema = rs.getString("TABLE_SCHEM");
				final String strTableName = rs.getString("TABLE_NAME");
				// final String strTableType = rs.getString("TABLE_TYPE");
				// final String strRemarks = rs.getString("REMARKS");
				// final String strTypeCatalog = rs.getString("TYPE_CAT");
				// final String strTypeSchema = rs.getString("TYPE_SCHEM");
				// final String strTypeName = rs.getString("TYPE_NAME");
				// final String strSelfReferencingColumnName = rs.getString("SELF_REFERENCING_COL_NAME");
				// final String strRefGeneration = rs.getString("REF_GENERATION");

				final Table table = new Table(schema, strTableName);

				readTable(schema, table);
				// readTable(metaData, schema, table);
				readTablePrimaryKey(metaData, schema, table);
			}
		}
		finally {
			DbUtils.closeQuietly(rs);
		}

		// traverse the tables after they have been read in to establish the foreign keys; required
		// to do after since the order of tables may not be in dependency order
		final Iterator<Table> iterTables = schema.getTables();
		while (iterTables.hasNext()) {
			final Table table = iterTables.next();

			readTableForeignKeys(metaData, schema, table);
			readTableIndices(metaData, schema, table);
		}
	}

	/**
	 * This method reads in the information about the named table.
	 * 
	 * @param table Table definition which contains the name of the Table.
	 * @throws SQLException
	 */
	private void readTable(final Schema schema, final Table table) throws SQLException {
		final String strSql =
			MessageFormat.format(m_sqlBuilder.getTableDefinitionSql(), schema.getName(), table.getName());

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSql);

			while (rs.next()) {
				final String strColumnName = rs.getString("COLUMN_NAME");
				// final String strDefault = rs.getString("COLUMN_DEFAULT");
				final boolean bNullable = "YES".equals(rs.getString("IS_NULLABLE"));
				final boolean bIdColumn = rs.getBoolean("ID_COLUMN");
				final String strDataType = rs.getString("DATA_TYPE");
				final int nColumnSize = rs.getInt("CHARACTER_MAXIMUM_LENGTH");

				final ColumnDefinition column =
					new ColumnDefinition(table, strColumnName, getSqlBuilder().getType(strDataType, bIdColumn));

				if (column.getType().hasLength()) {
					column.setLength(nColumnSize);
				}

				column.setRequired(!bNullable);
				// column.setDefault(strDefault);
			}
		}
		finally {
			DbUtils.closeQuietly(conn, stmt, rs);
		}
	}

	/**
	 * This method reads the tables foreign key constraints.
	 * 
	 * @throws SQLException
	 * @throws DatabaseElementException
	 */
	private void readTableForeignKeys(final DatabaseMetaData metaData, final Schema schema, final Table table)
			throws SQLException, DatabaseElementException {
		ResultSet rs = null;
		try {
			rs = metaData.getImportedKeys(schema.getName(), null, table.getName());

			while (rs.next()) {
				// final String strParentTableCatalog = rs.getString("PKTABLE_CAT");
				// PKTABLE_SCHEM String => primary key table schema being imported (may be null)
				final String strParentTableName = rs.getString("PKTABLE_NAME");
				final String strParentColumnName = rs.getString("PKCOLUMN_NAME");
				// FKTABLE_CAT String => foreign key table catalog (may be null)
				// FKTABLE_SCHEM String => foreign key table schema (may be null)
				// final String strChildName = rs.getString("FKTABLE_NAME"); // better be the passed in table
				final String strChildColumnName = rs.getString("FKCOLUMN_NAME");
				// final int nSequence = rs.getInt("KEY_SEQ");
				// UPDATE_RULE short => What happens to a foreign key when the primary key is updated:
				// * importedNoAction - do not allow update of primary key if it has been imported
				// * importedKeyCascade - change imported key to agree with primary key update
				// * importedKeySetNull - change imported key to NULL if its primary key has been updated
				// * importedKeySetDefault - change imported key to default values if its primary key has been updated
				// * importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x compatibility)
				// DELETE_RULE short => What happens to the foreign key when primary is deleted.
				// * importedKeyNoAction - do not allow delete of primary key if it has been imported
				// * importedKeyCascade - delete rows that import a deleted key
				// * importedKeySetNull - change imported key to NULL if its primary key has been deleted
				// * importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x compatibility)
				// * importedKeySetDefault - change imported key to default if its primary key has been deleted
				final String strKeyName = rs.getString("FK_NAME");
				// PK_NAME String => primary key name (may be null)
				// DEFERRABILITY short => can the evaluation of foreign key constraints be deferred until commit
				// * importedKeyInitiallyDeferred - see SQL92 for definition
				// * importedKeyInitiallyImmediate - see SQL92 for definition
				// * importedKeyNotDeferrable - see SQL92 for definition

				Constraint constraint = table.getConstraint(strKeyName);
				if (constraint == null) {
					constraint = new ForeignKeyConstraint();

					constraint.setName(strKeyName);

					table.add(constraint);
				}

				// add the column to the constraint
				final ForeignKey foreignKey = new ForeignKey();

				final Column columnChild = table.getColumn(strChildColumnName);
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
					((ColumnDefinition)columnChild).setType(ColumnTypeFactory.getFactory().get(
							ColumnTypeIdReference.TYPE));
				}
			}
		}
		finally {
			DbUtils.closeQuietly(rs);
		}
	}

	/**
	 * This method reads the table's indices.
	 * 
	 * @throws SQLException
	 * @throws DatabaseElementException
	 */
	private void readTableIndices(final DatabaseMetaData metaData, final Schema schema, final Table table)
			throws SQLException, DatabaseElementException {
		ResultSet rs = null;
		try {
			rs = metaData.getIndexInfo(schema.getName(), null, table.getName(), false, false);

			while (rs.next()) {
				// final String strCatalog = rs.getString("TABLE_CAT");
				// final String strSchema = rs.getString("TABLE_SCHEM");
				// final String strTable = rs.getString("TABLE_NAME");
				final boolean bNonUnique = rs.getBoolean("NON_UNIQUE");
				// final String strIndexCatalog = rs.getString("INDEX_QUALIFIER");
				final String strKeyName = rs.getString("INDEX_NAME");
				// final int nType = rs.getInt("TYPE");
				// final int nKeySequence = rs.getInt("ORDINAL_POSITION");
				final String strColumnName = rs.getString("COLUMN_NAME");
				// final String strDirection = rs.getString("ASC_OR_DESC");
				// final int nCardinality = rs.getInt("CARDINALITY");
				// final int nPages = rs.getInt("PAGES");
				// final String strFilterCondition = rs.getString("FILTER_CONDITION");

				if (strKeyName != null) {
					Constraint constraint = table.getConstraint(strKeyName);
					if (constraint == null) {
						constraint = new Index();

						constraint.setName(strKeyName);
						((Index)constraint).setUnique(!bNonUnique);

						table.add(constraint);
					}

					final Column column = table.getColumn(strColumnName);
					if (!constraint.hasColumn(column)) {
						constraint.addColumn(column);
					}
				}
			}
		}
		finally {
			DbUtils.closeQuietly(rs);
		}
	}

	/**
	 * This method reads the tables primary key constraints.
	 * 
	 * @throws SQLException
	 * @throws DatabaseElementException
	 */
	private void readTablePrimaryKey(final DatabaseMetaData metaData, final Schema schema, final Table table)
			throws SQLException, DatabaseElementException {
		ResultSet rs = null;
		try {
			rs = metaData.getPrimaryKeys(schema.getName(), null, table.getName());

			// TABLE_CAT String => table catalog (may be null)
			// TABLE_SCHEM String => table schema (may be null)
			// TABLE_NAME String => table name
			// COLUMN_NAME String => column name
			// KEY_SEQ short => sequence number within primary key
			// PK_NAME String => primary key name (may be null)

			PrimaryKeyConstraint key = null;

			while (rs.next()) {
				final String strColumnName = rs.getString("COLUMN_NAME");
				// final int nKeySequence = rs.getInt("KEY_SEQ");
				final String strKeyName = rs.getString("PK_NAME");

				if (key == null) {
					key = new PrimaryKeyConstraint();

					key.setName(strKeyName);

					table.add(key);
				}

				// add the column to the constraint
				key.addColumn(table.getColumn(strColumnName));
			}
		}
		finally {
			DbUtils.closeQuietly(rs);
		}
	}

	/**
	 * This method sets the builder to the passed in instance.
	 * 
	 * @param sqlBuilderImpl
	 */
	private void setBuilder(final SqlBuilder sqlBuilder) {
		m_sqlBuilder = sqlBuilder;
	}

	/**
	 * This method sets the SQL Builder instances based on the type of database.
	 * 
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	private void setSqlBuilder() throws SQLException, DatabaseException {
		Connection conn = null;

		try {
			conn = getConnection();
			final DatabaseMetaData metaData = conn.getMetaData();

			final String strDatabaseType = metaData.getDatabaseProductName();
			if ("MySQL".equals(strDatabaseType)) {
				setBuilder(new SqlMySqlBuilder());
			}
			else if (strDatabaseType.contains("Microsoft")) {
				setBuilder(new SqlServerBuilder());
			}
			else {
				throw new DatabaseException("Could not recognize database type '" + strDatabaseType + "'");
			}
		}
		finally {
			DbUtils.closeQuietly(conn);
		}
	}

	/**
	 * This method closes the use of a connection.
	 */
	public static void close(final Connection conn, final SqlExecution sqlExecution, final ResultSet rs) {
		DbUtils.closeQuietly(rs);
		if (sqlExecution != null) {
			sqlExecution.close();
		}
		DbUtils.closeQuietly(conn);
	}

	/**
	 * This class is the source for the connections. Default action is to throw an exception.
	 */
	public static class ConnectionSource {
		/**
		 * This method returns a connection from the source.
		 * 
		 * @throws SQLException
		 */
		public Connection getConnection() throws SQLException {
			throw new SQLException("Connection source was not found.");
		}

		/**
		 * This method gives the connection source a chance to test the validity of the initialization parameters.
		 * 
		 * @return Boolean value indicating if connections can be generated from this source.
		 */
		public boolean isValid() {
			boolean bValid = false;

			Connection conn = null;

			try {
				conn = getConnection();
				bValid = conn != null;
			}
			catch (final SQLException e) {
				m_log.info("Could not validate a connection for '" + this + "'.");
			}
			finally {
				DbUtils.closeQuietly(conn);
			}

			return bValid;
		}
	}

	/**
	 * This class is the source from connections from a data source.
	 */
	public static class DataConnectionSource extends ConnectionSource {
		/** This member holds the source of the connections. */
		private DataSource m_ds;

		/** This member holds the string name of the data source. */
		private String m_strName;

		/**
		 * This method constructs a source with the passed in data source.
		 */
		public DataConnectionSource(final DataSource ds) {
			m_ds = ds;
		}

		/**
		 * This method constructs a new data source given the name of the data source.
		 * 
		 * @throws NamingException
		 * @throws SQLException
		 */
		public DataConnectionSource(final String strName) throws NamingException, SQLException {
			m_strName = strName;
			final Context initContext = new InitialContext();
			final Context envContext = (Context)initContext.lookup("java:/comp/env");
			if (envContext != null) {
				m_ds = (DataSource)envContext.lookup(m_strName);
			}

			if (m_ds == null) {
				throw new SQLException("DataSource name of '" + strName + "' could not be resolved.");
			}
		}

		/**
		 * This method returns a connection from the source.
		 * 
		 * @throws SQLException
		 */
		@Override
		public Connection getConnection() throws SQLException {
			return m_ds.getConnection();
		}

		/**
		 * This method returns a string representation of the connection source.
		 */
		@Override
		public String toString() {
			return "Datasource: " + m_strName;
		}
	}

	/**
	 * This class constructs connections via the driver method. Explicit properties are used to determine the database
	 * to connection to.
	 */
	public static class DriverConnectionSource extends ConnectionSource {
		public static final String PROPERTY_JDBC_DATABASE = "jdbc.database";
		public static final String PROPERTY_JDBC_PASSWORD = "jdbc.password";
		public static final String PROPERTY_JDBC_URL = "jdbc.url";
		public static final String PROPERTY_JDBC_USER = "jdbc.user";

		/** This member holds the database/catalog name. */
		private String m_strDatabase;

		/** This member holds the database password. */
		private final String m_strPassword;

		/** This member holds the database URL. */
		private final String m_strUrl;

		/** This member holds the database user. */
		private final String m_strUser;

		/**
		 * This method constructs a driver source instance for the given parameters.
		 */
		public DriverConnectionSource(final String strUrl, final String strUser, final String strPassword) {
			this(strUrl, strUser, strPassword, null);
		}

		/**
		 * This method constructs a driver source instance for the given parameters.
		 */
		public DriverConnectionSource(final String strUrl, final String strUser, final String strPassword,
				final String strDatabase) {
			m_strUrl = strUrl;
			m_strUser = strUser;
			m_strPassword = strPassword;
			m_strDatabase = strDatabase;

			if (m_strDatabase == null) {
				final int nIndex = m_strUrl.lastIndexOf('/');
				if (nIndex >= 0) {
					m_strDatabase = m_strUrl.substring(nIndex + 1);
				}
			}
		}

		/**
		 * This method constructs a driver source instance by getting the required properties from the the properties
		 * class.
		 */
		public DriverConnectionSource(final StringProperties properties) {
			this(properties.getValue(PROPERTY_JDBC_URL), properties.getValue(PROPERTY_JDBC_USER), properties
					.getValue(PROPERTY_JDBC_PASSWORD), properties.getValue(PROPERTY_JDBC_DATABASE));
		}

		/**
		 * This method returns a connection from the source.
		 * 
		 * @throws SQLException
		 */
		@Override
		public Connection getConnection() throws SQLException {
			return DriverManager.getConnection(m_strUrl, m_strUser, m_strPassword);
		}

		/**
		 * This method returns a string representation of the connection source.
		 */
		@Override
		public String toString() {
			return "Driver with URL: " + m_strUrl;
		}

		static {
			// load all of the commonly known drivers
			loadDriver("com.mysql.jdbc.Driver");
			loadDriver("net.sourceforge.jtds.jdbc.Driver");
			loadDriver("oracle.jdbc.OracleDriver");
		}

		/**
		 * This method loads a driver by name. This is needed by the use of DriverManager.
		 * 
		 * @param strDriverClassName String containing the full class name of the driver to be loaded
		 */
		private static void loadDriver(final String strDriverClassName) {
			try {
				Class.forName(strDriverClassName);
			}
			catch (final ClassNotFoundException e) {
				m_log.info("Could not load driver '" + strDriverClassName + "'");
			}
		}
	}
}

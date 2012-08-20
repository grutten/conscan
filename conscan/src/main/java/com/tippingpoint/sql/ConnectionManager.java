package com.tippingpoint.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.database.DatabaseElementException;
import com.tippingpoint.database.DatabaseException;
import com.tippingpoint.database.IdAutoFactory;
import com.tippingpoint.database.IdFactory;
import com.tippingpoint.database.IdGuidFactory;
import com.tippingpoint.database.Schema;
import com.tippingpoint.sql.base.SqlExecution;
import com.tippingpoint.sql.base.SqlManager;
import com.tippingpoint.sql.mysql.SqlManagerMySql;
import com.tippingpoint.sql.oracle.SqlManagerOracle;
import com.tippingpoint.sql.sqlserver.SqlManagerSqlServer;
import com.tippingpoint.utilities.StringProperties;

/**
 * This class manages returning a connection.
 */
public final class ConnectionManager {
	private static final Log m_log = LogFactory.getLog(ConnectionManager.class);

	private static final String PROPERTY_USE_AUTOINCREMENT = "db.use.autoincrement";

	/** This member holds the source of the connections. */
	private ConnectionSource m_connections = new ConnectionSource();

	/** This member holds the ID factory used for this connection. */
	private IdFactory m_idFactory;

	/** This member holds the schema definition of the tables found in the database associated with this manager. */
	private Schema m_schema;

	/** This member holds the SQL manager used to generate and execute SQL statements against the database. */
	private SqlManager m_sqlManager;

	/**
	 * This method constructs an new connection manager.
	 * 
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	public ConnectionManager(final ConnectionSource connections) throws SQLException, DatabaseException {
		this(connections, null);
	}

	/**
	 * This method constructs an new connection manager.
	 * 
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	public ConnectionManager(ConnectionSource connections, final StringProperties properties) throws SQLException,
			DatabaseException {
		// make sure connection source is specified so a known error will occur
		if (connections == null) {
			connections = new ConnectionSource();
		}

		m_connections = connections;

		setIdFactory(properties);
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
	 * This method returns the connection source for the manager.
	 */
	public ConnectionSource getConnectionSource() {
		return m_connections;
	}

	/**
	 * This method returns the Id factory instance.
	 */
	public IdFactory getIdFactory() {
		return m_idFactory;
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
	 * @throws SqlExecutionException
	 * @throws DatabaseElementException
	 * @throws SQLException
	 */
	public Schema getSchema(final String strSchemaName) throws SqlExecutionException, DatabaseElementException,
			SQLException {
		Schema schema = null;

		Connection conn = null;
		try {
			conn = getConnection();

			// final DatabaseMetaData metaData = conn.getMetaData();

			// schema = new Schema(strSchemaName);

			// readSchema(metaData, schema);

			schema = m_sqlManager.getSqlSchema().getSchema(conn, strSchemaName);
		}
		finally {
			DbUtils.closeQuietly(conn);
		}

		return schema;
	}

	/**
	 * This method returns a SQL manager class specific to the database defined by the connections.
	 */
	public SqlManager getSqlManager() {
		return m_sqlManager;
	}

	/**
	 * This method sets the id factory to that passed in.
	 * 
	 * @param idFactory IdFactory to be used with this connection manager.
	 */
	public void setIdFactory(final IdFactory idFactory) {
		m_idFactory = idFactory;
	}

	/**
	 * This method sets the schema associated with the connections associated with this manager.
	 */
	public void setSchema(final Schema schema) {
		m_schema = schema;
	}

	/**
	 * This method sets the ID factory. By default, a GUID based factory is used, but auto-increments can be used.
	 * 
	 * @param properties Properties used to configure the application
	 */
	private void setIdFactory(final StringProperties properties) {
		boolean bUseAutoIncrement = false;
		if (properties != null) {
			bUseAutoIncrement = new Boolean(properties.getValue(PROPERTY_USE_AUTOINCREMENT, Boolean.FALSE.toString()));
		}

		if (bUseAutoIncrement) {
			setIdFactory(new IdAutoFactory());
		}
		else {
			setIdFactory(new IdGuidFactory());
		}
	}

	/**
	 * This method sets the manager to used for SQL executions.
	 * 
	 * @param sqlManager SqlManager instance for this connection.
	 */
	private void setManager(final SqlManager sqlManager) {
		m_sqlManager = sqlManager;

		if (m_sqlManager != null) {
			m_sqlManager.setConnectionManager(this);
		}
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
				setManager(new SqlManagerMySql(m_idFactory));
			}
			else if (strDatabaseType.contains("Microsoft")) {
				setManager(new SqlManagerSqlServer(m_idFactory));
			}
			else if (strDatabaseType.contains("Oracle")) {
				setManager(new SqlManagerOracle(m_idFactory));
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
		 * This method returns the name of the schema.
		 */
		public String getSchema() {
			return null;
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
				m_log.info("Could not validate a connection for '" + this + "'.", e);
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
		 * This method returns the name of the schema.
		 */
		@Override
		public String getSchema() {
			return m_strName; // FUTURE: not sure if this is the correct name
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
			if (strUrl == null || strUrl.length() == 0) {
				throw new IllegalArgumentException("Could not establish a driver connection with no URL.");
			}

			if (strUser == null || strUser.length() == 0) {
				throw new IllegalArgumentException("Could not establish a driver connection with no user.");
			}

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
		 * This method returns the name of the schema.
		 */
		@Override
		public String getSchema() {
			return m_strDatabase;
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

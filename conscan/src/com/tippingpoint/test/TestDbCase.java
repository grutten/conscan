package com.tippingpoint.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.tippingpoint.database.DatabaseElementException;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;
import com.tippingpoint.sql.SchemaComparison;
import com.tippingpoint.sql.SqlExecution;

/**
 * This class is a common base class for unit test that relies on having a connection to a database. Unit test
 * properties are used to initialize the connection manager.
 */
public abstract class TestDbCase extends TestCommonCase {
	public static final String UNIT_TEST_SCHEMA_NAME = "unittest";

	/**
	 * This method is used to dump the existing database schema and add the new schema.
	 * 
	 * @throws SQLException
	 * @throws DatabaseElementException
	 * @throws SqlExecutionException
	 * @throws SqlBuilderException
	 */
	protected void refreshDb(final Schema schemaNew) throws DatabaseElementException, SQLException,
			SqlBuilderException, SqlExecutionException {
		// get the schema based on what is in the database
		final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
		final Schema schemaCurrent = manager.getSchema(UNIT_TEST_SCHEMA_NAME);

		if (schemaCurrent.getTableCount() > 0) {
			dropSchema(schemaCurrent);
		}

		// compare against an empty schema to generate all new tables
		final SchemaComparison comparison = new SchemaComparison(new Schema(schemaNew.getName()), schemaNew);

		comparison.process(manager);
	}

	/**
	 * This method is called prior to each test case method being called.
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		final ConnectionManagerFactory factory = ConnectionManagerFactory.getFactory();
		if (factory.getDefaultManager() == null) {
			// set a new connection based on values found in the properties
			// determine the connection to the database
			final ConnectionManager.ConnectionSource connectionSource =
				new ConnectionManager.DriverConnectionSource(m_unitTestProperties);
			if (!connectionSource.isValid()) {
				fail("Could not configure a database connection.");
			}

			final ConnectionManager connectionManager = new ConnectionManager(connectionSource);

			// register the connection manager
			factory.register(UNIT_TEST_SCHEMA_NAME, connectionManager);
		}
	}

	/**
	 * This method drops the tables in the schema by creating a list of tables, traverse that list until all tables in
	 * the schema have been deleted.
	 * 
	 * @param schema Schema to be dropped
	 * @throws SQLException
	 */
	private void dropSchema(final Schema schema) throws SQLException {
		final List<Table> listTables = new ArrayList<Table>(schema.getTableCount());

		final Iterator<Table> iterTables = schema.getTables();
		while (iterTables.hasNext()) {
			listTables.add(iterTables.next());
		}

		final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();

		Connection conn = null;

		try {
			conn = manager.getConnection();

			int nPreviousTableCount = listTables.size() + 1; // + 1 == allow the first pass to execute
			while (listTables.size() > 0 && nPreviousTableCount > listTables.size()) {
				nPreviousTableCount = listTables.size();
				for (int nIndex = listTables.size() - 1; nIndex >= 0; --nIndex) {
					final Table table = listTables.get(nIndex);
					final SqlDrop sqlDrop = manager.getSqlBuilder().getDrop(table);

					SqlExecution sqlExecution = null;

					try {
						sqlExecution = sqlDrop.getExecution();
						sqlExecution.executeUpdate(conn);

						// successfully dropped the table, so remove it from the list
						listTables.remove(nIndex);
					}
					catch (final SqlExecutionException e) {
						// ignore the cannot drop messages
					}
					finally {
						ConnectionManager.close(null, sqlExecution, null);
					}
				}
			}

			if (listTables.size() > 0) {
				fail("Cannot drop the following tables: " + listTables);
			}
		}
		catch (final SqlBuilderException e) {
			e.printStackTrace();
			fail("SQL Builder Exception: " + e.getMessage());
		}
		finally {
			ConnectionManager.close(conn, null, null);
		}
	}
}

package com.tippingpoint.sql;

import java.io.StringReader;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.IdAutoFactory;
import com.tippingpoint.database.IdGuidFactory;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.database.parser.Parser;
import com.tippingpoint.sql.base.SqlExecution;
import com.tippingpoint.sql.base.SqlManager;
import com.tippingpoint.sql.mysql.SqlManagerMySql;
import com.tippingpoint.sql.sqlserver.SqlManagerSqlServer;
import com.tippingpoint.test.TestCommonCase;

/**
 * TestSqlBuilder
 */
public final class TestSqlBuilder extends TestCommonCase {
	private static final String DB =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?><schema name=\"mytable\"><table name=\"demographic\">"
				+ "<column name=\"userid\" type=\"id\"/><column name=\"firstName\" type=\"string\" length=\"100\"/>"
				+ "<column name=\"lastName\" type=\"string\" length=\"100\"/><column name=\"creation\" type=\"date\"/>"
				+ "<constraint name=\"pk_demographic\" type=\"primary\"><column name=\"userid\"/></constraint></table>"
				+ "<table name=\"activity\"><column name=\"activityid\" type=\"id\"/>"
				+ "<column name=\"title\" type=\"string\" length=\"200\"/><column name=\"description\" type=\"text\"/>"
				+ "<column name=\"creation\" type=\"date\"/><column name=\"lastmodified\" type=\"date\"/>"
				+ "<constraint name=\"pk_activity\" type=\"primary\"><column name=\"activityid\"/></constraint></table>"
				+ "<table name=\"useractivity\"><column name=\"userid\" type=\"idref\" required=\"true\"/>"
				+ "<column name=\"activityid\" type=\"idref\" required=\"true\"/>"
				+ "<constraint name=\"fk_useractivity_demographic\" type=\"foreign\"><column name=\"userid\"/>"
				+ "<table name=\"demographic\"><column name=\"userid\"/></table></constraint>"
				+ "<constraint name=\"fk_useractivity_activity\" type=\"foreign\"><column name=\"activityid\"/>"
				+ "<table name=\"activity\"><column name=\"activityid\"/></table></constraint></table></schema>";

	/** This member contains the schema used to test the various SQL generation statements. */
	private Schema m_schema;

	/**
	 * This method tests the basics.
	 */
	public void testBasics() {
		try {
			final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
			assertNotNull(manager);

			final SqlManager sqlManager = manager.getSqlManager();
			assertNotNull(sqlManager);

			final Table tableDemographic = m_schema.getTable("demographic");
			assertNotNull(tableDemographic);

			final SqlQuery sqlQuery = new SqlQuery();
			assertNotNull(sqlQuery);

			sqlQuery.add(tableDemographic, true);

			check("SELECT demographic.userid, demographic.firstName, demographic.lastName, demographic.creation "
					+ "FROM demographic", sqlManager, sqlQuery);

			sqlQuery.add(new ValueCondition(tableDemographic.getColumn("userid"), Operation.EQUALS, "newuser"));

			check("SELECT demographic.userid, demographic.firstName, demographic.lastName, demographic.creation "
					+ "FROM demographic WHERE demographic.userid = ?", sqlManager, sqlQuery);

			SqlInsert sqlInsert = new SqlInsert(tableDemographic);
			assertNotNull(sqlInsert);

			sqlInsert.add(new ParameterizedValue(tableDemographic.getColumn("firstName"), null));
			sqlInsert.add(new ParameterizedValue(tableDemographic.getColumn("lastName"), null));
			sqlInsert.add(new ParameterizedValue(tableDemographic.getColumn("creation"), null));

			check("INSERT INTO demographic(firstName, lastName, creation) VALUES(?, ?, ?)", sqlManager, sqlInsert);

			sqlInsert = new SqlInsert(tableDemographic);
			assertNotNull(sqlInsert);

			sqlInsert.addColumnsForTable();

			check("INSERT INTO demographic(userid, firstName, lastName, creation) VALUES(?, ?, ?, ?)", sqlManager,
					sqlInsert);

			final SqlUpdate sqlUpdate = new SqlUpdate(tableDemographic);
			assertNotNull(sqlUpdate);

			sqlUpdate.add(new ParameterizedValue(tableDemographic.getColumn("firstName"), "Joe"));
			sqlUpdate.add(new ParameterizedValue(tableDemographic.getColumn("lastName"), "Doe"));
			sqlUpdate.add(new ValueCondition(tableDemographic.getColumn("userid"), Operation.EQUALS, "bbb"));

			check("UPDATE demographic SET firstName = ?, lastName = ? WHERE demographic.userid = ?", sqlManager,
					sqlUpdate);

			// test joined tables
			final SqlQuery sqlQuery2 = new SqlQuery();
			assertNotNull(sqlQuery2);

			final Table tableUserActivity = m_schema.getTable("useractivity");
			assertNotNull(tableUserActivity);

			sqlQuery2.add(tableDemographic, true);
			sqlQuery2.add(tableUserActivity);

			check("SELECT demographic.userid, demographic.firstName, demographic.lastName, demographic.creation "
					+ "FROM demographic, useractivity WHERE demographic.userid = useractivity.userid", sqlManager,
					sqlQuery2);

			// test many to many joined tables
			final SqlQuery sqlQuery3 = new SqlQuery();
			assertNotNull(sqlQuery3);

			final Table tableActivity = m_schema.getTable("activity");
			assertNotNull(tableActivity);

			sqlQuery3.add(tableDemographic, true);
			sqlQuery3.add(tableUserActivity);
			sqlQuery3.add(tableActivity, true);

			check(
					"SELECT demographic.userid, demographic.firstName, demographic.lastName, demographic.creation, activity.activityid, activity.title, "
							+ "activity.description, activity.creation, activity.lastmodified FROM demographic, useractivity, activity "
							+ "WHERE demographic.userid = useractivity.userid AND activity.activityid = useractivity.activityid",
					sqlManager, sqlQuery3);

			// test many to many with non-specified tables
			final SqlQuery sqlQuery4 = new SqlQuery();
			assertNotNull(sqlQuery4);

			sqlQuery4.setAssociativeJoins(true);

			sqlQuery4.add(tableDemographic, true);
			sqlQuery4.add(tableActivity, true);

			check(
					"SELECT demographic.userid, demographic.firstName, demographic.lastName, demographic.creation, activity.activityid, activity.title, "
							+ "activity.description, activity.creation, activity.lastmodified FROM demographic, activity, useractivity "
							+ "WHERE demographic.userid = useractivity.userid AND activity.activityid = useractivity.activityid",
					sqlManager, sqlQuery4);
		}
		catch (final SqlManagerException e) {
			e.printStackTrace();
			assertFalse(e.toString(), true);
		}
		catch (final SqlBuilderException e) {
			e.printStackTrace();
			assertFalse(e.toString(), true);
		}
	}

	/**
	 * This method tests the GUID version of the identities.
	 */
	public void testGuidIdentities() {

	}

	/**
	 * This method tests MySQL Server specific SQL statements.
	 */
	public void testMySqlServer() {
		final SqlManager sqlManager = new SqlManagerMySql(new IdAutoFactory());
		assertNotNull(sqlManager);

		// test creating a table
		final Table tableActivity = m_schema.getTable("activity");
		assertNotNull(tableActivity);

		try {
			SqlCreate sqlCreate = new SqlCreate(tableActivity);
			assertNotNull(sqlCreate);

			check("CREATE TABLE activity(activityid INTEGER AUTO_INCREMENT NOT NULL, title VARCHAR(200) NULL, "
					+ "description TEXT NULL, creation DATETIME NULL, lastmodified DATETIME NULL, "
					+ "CONSTRAINT pk_activity PRIMARY KEY (activityid))", sqlManager, sqlCreate);

			// test creating a table
			final Table tableDemographic = m_schema.getTable("demographic");
			assertNotNull(tableDemographic);

			sqlCreate = new SqlCreate(tableDemographic);
			assertNotNull(sqlCreate);

			check("CREATE TABLE demographic(userid INTEGER AUTO_INCREMENT NOT NULL, "
					+ "firstName VARCHAR(100) NULL, lastName VARCHAR(100) NULL, creation DATETIME NULL, "
					+ "CONSTRAINT pk_demographic PRIMARY KEY (userid))", sqlManager, sqlCreate);

			// use a second table to add a column to the above table
			final ColumnDefinition columnDescription = tableActivity.getColumn("description");
			assertNotNull(columnDescription);

			final SqlAlter sqlAlter = new SqlAlter(tableDemographic);
			assertNotNull(sqlAlter);

			sqlAlter.add(columnDescription);

			check("ALTER TABLE demographic ADD description TEXT NULL", sqlManager, sqlAlter);

			// use an existing column in the table to emulate modifying a table.
			final ColumnDefinition columnCreation = tableDemographic.getColumn("creation");
			assertNotNull(columnCreation);

			sqlAlter.add(columnCreation);

			check("ALTER TABLE demographic ADD description TEXT NULL, MODIFY creation DATETIME NULL", sqlManager,
					sqlAlter);

			// try a table with foreign keys
			final Table tableUserActivity = m_schema.getTable("useractivity");
			assertNotNull(tableUserActivity);

			sqlCreate = new SqlCreate(tableUserActivity);
			assertNotNull(sqlCreate);

			check("CREATE TABLE useractivity(userid INTEGER NOT NULL, activityid INTEGER NOT NULL, "
					+ "CONSTRAINT fk_useractivity_demographic FOREIGN KEY (userid) REFERENCES demographic (userid), "
					+ "CONSTRAINT fk_useractivity_activity FOREIGN KEY (activityid) REFERENCES activity (activityid))",
					sqlManager, sqlCreate);
		}
		catch (final SqlBuilderException e) {
			e.printStackTrace();
			assertFalse(e.toString(), true);
		}
		catch (final SqlManagerException e) {
			e.printStackTrace();
			assertFalse(e.toString(), true);
		}
	}

	/**
	 * This method tests MySQL Server specific SQL statements.
	 */
	public void testMySqlServerGuid() {
		final SqlManager sqlManager = new SqlManagerMySql(new IdGuidFactory());
		assertNotNull(sqlManager);

		// test creating a table
		final Table tableActivity = m_schema.getTable("activity");
		assertNotNull(tableActivity);

		try {
			SqlCreate sqlCreate = new SqlCreate(tableActivity);
			assertNotNull(sqlCreate);

			check("CREATE TABLE activity(activityid CHAR(32) NOT NULL, title VARCHAR(200) NULL, "
					+ "description TEXT NULL, creation DATETIME NULL, lastmodified DATETIME NULL, "
					+ "CONSTRAINT pk_activity PRIMARY KEY (activityid))", sqlManager, sqlCreate);

			// test creating a table
			final Table tableDemographic = m_schema.getTable("demographic");
			assertNotNull(tableDemographic);

			sqlCreate = new SqlCreate(tableDemographic);
			assertNotNull(sqlCreate);

			check("CREATE TABLE demographic(userid CHAR(32) NOT NULL, "
					+ "firstName VARCHAR(100) NULL, lastName VARCHAR(100) NULL, creation DATETIME NULL, "
					+ "CONSTRAINT pk_demographic PRIMARY KEY (userid))", sqlManager, sqlCreate);

			// use a second table to add a column to the above table
			final ColumnDefinition columnDescription = tableActivity.getColumn("description");
			assertNotNull(columnDescription);

			final SqlAlter sqlAlter = new SqlAlter(tableDemographic);
			assertNotNull(sqlAlter);

			sqlAlter.add(columnDescription);

			check("ALTER TABLE demographic ADD description TEXT NULL", sqlManager, sqlAlter);

			// use an existing column in the table to emulate modifying a table.
			final ColumnDefinition columnCreation = tableDemographic.getColumn("creation");
			assertNotNull(columnCreation);

			sqlAlter.add(columnCreation);

			check("ALTER TABLE demographic ADD description TEXT NULL, MODIFY creation DATETIME NULL", sqlManager,
					sqlAlter);

			// try a table with foreign keys
			final Table tableUserActivity = m_schema.getTable("useractivity");
			assertNotNull(tableUserActivity);

			sqlCreate = new SqlCreate(tableUserActivity);
			assertNotNull(sqlCreate);

			check("CREATE TABLE useractivity(userid CHAR(32) NOT NULL, activityid CHAR(32) NOT NULL, "
					+ "CONSTRAINT fk_useractivity_demographic FOREIGN KEY (userid) REFERENCES demographic (userid), "
					+ "CONSTRAINT fk_useractivity_activity FOREIGN KEY (activityid) REFERENCES activity (activityid))",
					sqlManager, sqlCreate);
		}
		catch (final SqlBuilderException e) {
			e.printStackTrace();
			assertFalse(e.toString(), true);
		}
		catch (final SqlManagerException e) {
			e.printStackTrace();
			assertFalse(e.toString(), true);
		}
	}

	/**
	 * This method tests SQL Server specific SQL statements.
	 */
	public void testSqlServer() {
		final SqlManager sqlManager = new SqlManagerSqlServer(new IdAutoFactory());
		assertNotNull(sqlManager);

		// test creating a table
		final Table tableActivity = m_schema.getTable("activity");
		assertNotNull(tableActivity);

		try {
			SqlCreate sqlCreate = new SqlCreate(tableActivity);
			assertNotNull(sqlCreate);

			check("CREATE TABLE activity(activityid INTEGER IDENTITY NOT NULL, title VARCHAR(200) NULL, "
					+ "description TEXT NULL, creation DATETIME NULL, lastmodified DATETIME NULL, "
					+ "CONSTRAINT pk_activity PRIMARY KEY (activityid))", sqlManager, sqlCreate);

			// test creating a table
			final Table tableDemographic = m_schema.getTable("demographic");
			assertNotNull(tableDemographic);

			sqlCreate = new SqlCreate(tableDemographic);
			assertNotNull(sqlCreate);

			check("CREATE TABLE demographic(userid INTEGER IDENTITY NOT NULL, firstName VARCHAR(100) NULL, "
					+ "lastName VARCHAR(100) NULL, creation DATETIME NULL, CONSTRAINT pk_demographic "
					+ "PRIMARY KEY (userid))", sqlManager, sqlCreate);

			// use a second table to add a column to the above table
			final ColumnDefinition columnDescription = tableActivity.getColumn("description");
			assertNotNull(columnDescription);

			final SqlAlter sqlAlter = new SqlAlter(tableDemographic);
			assertNotNull(sqlAlter);

			sqlAlter.add(columnDescription);

			check("ALTER TABLE demographic ADD description TEXT NULL", sqlManager, sqlAlter);

			// use an existing column in the table to emulate modifying a table.
			final ColumnDefinition columnCreation = tableDemographic.getColumn("creation");
			assertNotNull(columnCreation);

			sqlAlter.add(columnCreation);

			check("ALTER TABLE demographic ADD description TEXT NULL, ALTER COLUMN creation DATETIME NULL", sqlManager,
					sqlAlter);

			// try a table with foreign keys
			final Table tableUserActivity = m_schema.getTable("useractivity");
			assertNotNull(tableUserActivity);

			sqlCreate = new SqlCreate(tableUserActivity);
			assertNotNull(sqlCreate);

			check("CREATE TABLE useractivity(userid INTEGER NOT NULL, activityid INTEGER NOT NULL, "
					+ "CONSTRAINT fk_useractivity_demographic FOREIGN KEY (userid) REFERENCES demographic (userid), "
					+ "CONSTRAINT fk_useractivity_activity FOREIGN KEY (activityid) REFERENCES activity (activityid))",
					sqlManager, sqlCreate);
		}
		catch (final SqlBuilderException e) {
			e.printStackTrace();
			assertFalse(e.toString(), true);
		}
		catch (final SqlManagerException e) {
			e.printStackTrace();
			assertFalse(e.toString(), true);
		}
	}

	/**
	 * This method tests SQL Server specific SQL statements.
	 */
	public void testSqlServerGuid() {
		final SqlManager sqlManager = new SqlManagerSqlServer(new IdGuidFactory());
		assertNotNull(sqlManager);

		// test creating a table
		final Table tableActivity = m_schema.getTable("activity");
		assertNotNull(tableActivity);

		try {
			SqlCreate sqlCreate = new SqlCreate(tableActivity);
			assertNotNull(sqlCreate);

			check("CREATE TABLE activity(activityid CHAR(32) NOT NULL, title VARCHAR(200) NULL, "
					+ "description TEXT NULL, creation DATETIME NULL, lastmodified DATETIME NULL, "
					+ "CONSTRAINT pk_activity PRIMARY KEY (activityid))", sqlManager, sqlCreate);

			// test creating a table
			final Table tableDemographic = m_schema.getTable("demographic");
			assertNotNull(tableDemographic);

			sqlCreate = new SqlCreate(tableDemographic);
			assertNotNull(sqlCreate);

			check("CREATE TABLE demographic(userid CHAR(32) NOT NULL, firstName VARCHAR(100) NULL, "
					+ "lastName VARCHAR(100) NULL, creation DATETIME NULL, CONSTRAINT pk_demographic "
					+ "PRIMARY KEY (userid))", sqlManager, sqlCreate);

			// use a second table to add a column to the above table
			final ColumnDefinition columnDescription = tableActivity.getColumn("description");
			assertNotNull(columnDescription);

			final SqlAlter sqlAlter = new SqlAlter(tableDemographic);
			assertNotNull(sqlAlter);

			sqlAlter.add(columnDescription);

			check("ALTER TABLE demographic ADD description TEXT NULL", sqlManager, sqlAlter);

			// use an existing column in the table to emulate modifying a table.
			final ColumnDefinition columnCreation = tableDemographic.getColumn("creation");
			assertNotNull(columnCreation);

			sqlAlter.add(columnCreation);

			check("ALTER TABLE demographic ADD description TEXT NULL, ALTER COLUMN creation DATETIME NULL", sqlManager,
					sqlAlter);

			// try a table with foreign keys
			final Table tableUserActivity = m_schema.getTable("useractivity");
			assertNotNull(tableUserActivity);

			sqlCreate = new SqlCreate(tableUserActivity);
			assertNotNull(sqlCreate);

			check("CREATE TABLE useractivity(userid CHAR(32) NOT NULL, activityid CHAR(32) NOT NULL, "
					+ "CONSTRAINT fk_useractivity_demographic FOREIGN KEY (userid) REFERENCES demographic (userid), "
					+ "CONSTRAINT fk_useractivity_activity FOREIGN KEY (activityid) REFERENCES activity (activityid))",
					sqlManager, sqlCreate);
		}
		catch (final SqlBuilderException e) {
			e.printStackTrace();
			assertFalse(e.toString(), true);
		}
		catch (final SqlManagerException e) {
			e.printStackTrace();
			assertFalse(e.toString(), true);
		}
	}

	/**
	 * This method is called to initialize the test.
	 * 
	 * @throws Exception
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		m_schema = Parser.parse(new StringReader(DB));
	}

	/**
	 * This method checks the execution of a command.
	 * 
	 * @throws SqlManagerException
	 * @throws SqlBuilderException
	 */
	private void check(final String strSql, final SqlManager sqlManager, final Command sqlCommand)
			throws SqlManagerException, SqlBuilderException {
		final SqlExecution execution = sqlManager.getExecution(sqlCommand);
		assertNotNull(execution);

		assertEquals(strSql, execution.getSql());
	}
}

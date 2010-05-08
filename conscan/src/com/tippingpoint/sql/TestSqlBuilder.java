package com.tippingpoint.sql;

import java.io.StringReader;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.database.parser.Parser;
import com.tippingpoint.sql.base.SqlManager;
import com.tippingpoint.sql.mysql.SqlManagerMySql;
import com.tippingpoint.sql.sqlserver.SqlManagerSqlServer;
import com.tippingpoint.test.TestCommonCase;

/**
 * TestSqlBuilder
 */
public final class TestSqlBuilder extends TestCommonCase {
	private static final String DB =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?><Schema name=\"myschema\"><Table name=\"demographic\">"
				+ "<Column name=\"userid\" type=\"id\"/><Column name=\"firstName\" type=\"string\" length=\"100\"/>"
				+ "<Column name=\"lastName\" type=\"string\" length=\"100\"/><Column name=\"creation\" type=\"date\"/>"
				+ "<Constraint name=\"pk_demographic\" type=\"primary\"><Column name=\"userid\"/></Constraint></Table>"
				+ "<Table name=\"activity\"><Column name=\"activityid\" type=\"id\"/>"
				+ "<Column name=\"title\" type=\"string\" length=\"200\"/><Column name=\"description\" type=\"text\"/>"
				+ "<Column name=\"creation\" type=\"date\"/><Column name=\"lastmodified\" type=\"date\"/>"
				+ "<Constraint name=\"pk_activity\" type=\"primary\"><Column name=\"activityid\"/></Constraint></Table>"
				+ "<Table name=\"useractivity\"><Column name=\"userid\" type=\"idref\" required=\"true\"/>"
				+ "<Column name=\"activityid\" type=\"idref\" required=\"true\"/>"
				+ "<Constraint name=\"fk_useractivity_demographic\" type=\"foreign\"><Column name=\"userid\"/>"
				+ "<Table name=\"demographic\"><Column name=\"userid\"/></Table></Constraint>"
				+ "<Constraint name=\"fk_useractivity_activity\" type=\"foreign\"><Column name=\"activityid\"/>"
				+ "<Table name=\"activity\"><Column name=\"activityid\"/></Table></Constraint></Table></Schema>";

	/** This member contains the schema used to test the various SQL generation statements. */
	private Schema m_schema;

	/**
	 * This method tests the basics.
	 */
/*	
	public void testBasics() {
		SqlManager sqlManager = new SqlManagerSqlServer();
		assertNotNull(sqlManager);
		
		final Table tableDemographic = m_schema.getTable("demographic");
		assertNotNull(tableDemographic);

		final SqlQuery sqlQuery = sqlManager.getQuery();
		assertNotNull(sqlQuery);

		sqlQuery.add(tableDemographic, true);

		assertEquals(
				"SELECT demographic.userid, demographic.firstName, demographic.lastName, demographic.creation FROM demographic",
				sqlQuery.toString());

		sqlQuery.add(new ValueCondition(tableDemographic.getColumn("userid"), Operation.EQUALS, "newuser"));

		assertEquals(
				"SELECT demographic.userid, demographic.firstName, demographic.lastName, demographic.creation FROM demographic "
						+ "WHERE demographic.userid = ?", sqlQuery.toString());

		SqlInsert sqlInsert = sqlManager.getInsert(tableDemographic);
		assertNotNull(sqlInsert);

		sqlInsert.add(new ParameterizedValue(tableDemographic.getColumn("firstName"), null));
		sqlInsert.add(new ParameterizedValue(tableDemographic.getColumn("lastName"), null));
		sqlInsert.add(new ParameterizedValue(tableDemographic.getColumn("creation"), null));

		assertEquals("INSERT INTO demographic(firstName, lastName, creation) VALUES(?, ?, ?)", sqlInsert.toString());

		sqlInsert = sqlManager.getInsert(tableDemographic);
		assertNotNull(sqlInsert);

		sqlInsert.addColumnsForTable();

		assertEquals("INSERT INTO demographic(firstName, lastName, creation) VALUES(?, ?, ?)", sqlInsert.toString());

		final SqlUpdate sqlUpdate = sqlManager.getUpdate(tableDemographic);
		assertNotNull(sqlUpdate);

		sqlUpdate.add(new ParameterizedValue(tableDemographic.getColumn("firstName"), "Joe"));
		sqlUpdate.add(new ParameterizedValue(tableDemographic.getColumn("lastName"), "Doe"));
		sqlUpdate.add(new ValueCondition(tableDemographic.getColumn("userid"), Operation.EQUALS, "bbb"));

		assertEquals("UPDATE demographic SET firstName = ?, lastName = ? WHERE demographic.userid = ?", sqlUpdate
				.toString());

		// test joined tables
		final SqlQuery sqlQuery2 = sqlManager.getQuery();
		assertNotNull(sqlQuery2);

		final Table tableUserActivity = m_schema.getTable("useractivity");
		assertNotNull(tableUserActivity);

		sqlQuery2.add(tableUser, true);
		sqlQuery2.add(tableUserActivity);

		assertEquals("SELECT demographic.userid, demographic.firstName, demographic.lastName, demographic.creation "
				+ "FROM demographic, useractivity WHERE demographic.userid = useractivity.userid", sqlQuery2.toString());

		// test many to many joined tables
		final SqlQuery sqlQuery3 = sqlManager.getQuery();
		assertNotNull(sqlQuery3);

		final Table tableActivity = m_schema.getTable("activity");
		assertNotNull(tableActivity);

		sqlQuery3.add(tableDemographic, true);
		sqlQuery3.add(tableUserActivity);
		sqlQuery3.add(tableActivity, true);

		assertEquals(
				"SELECT demographic.userid, demographic.firstName, demographic.lastName, demographic.creation, activity.activityid, activity.title, "
						+ "activity.description, activity.creation, activity.lastmodified FROM demographic, useractivity, activity "
						+ "WHERE demographic.userid = useractivity.userid AND activity.activityid = useractivity.activityid",
				sqlQuery3.toString());

		// test many to many with non-specified tables
		final SqlQuery sqlQuery4 = sqlManager.getQuery();
		assertNotNull(sqlQuery4);

		sqlQuery4.setAssociativeJoins(true);

		sqlQuery4.add(tableDemographic, true);
		sqlQuery4.add(tableActivity, true);

		assertEquals(
				"SELECT demographic.userid, demographic.firstName, demographic.lastName, demographic.creation, activity.activityid, activity.title, "
						+ "activity.description, activity.creation, activity.lastmodified FROM demographic, activity, useractivity "
						+ "WHERE demographic.userid = useractivity.userid AND activity.activityid = useractivity.activityid",
				sqlQuery4.toString());
	}
*/

	/**
	 * This method tests MySQL Server specific SQL statements.
	 */
	public void testMySqlServer() {
		SqlManager sqlManager = new SqlManagerMySql();
		assertNotNull(sqlManager);

		// test creating a table
		final Table tableActivity = m_schema.getTable("activity");
		assertNotNull(tableActivity);

		try {
			SqlCreate sqlCreate = new SqlCreate(tableActivity);
			assertNotNull(sqlCreate);
			
			com.tippingpoint.sql.base.SqlExecution execution = sqlManager.getExecution(sqlCreate);
			assertNotNull(execution);
	
			assertEquals("CREATE TABLE activity(activityid INTEGER AUTO_INCREMENT NOT NULL, title VARCHAR(200) NULL, "
					+ "description TEXT NULL, creation DATETIME NULL, lastmodified DATETIME NULL, "
					+ "CONSTRAINT pk_activity PRIMARY KEY (activityid))", execution.getSql());
	
			// test creating a table
			final Table tableDemographic = m_schema.getTable("demographic");
			assertNotNull(tableDemographic);

			sqlCreate = new SqlCreate(tableDemographic);
			assertNotNull(sqlCreate);
			
			execution = sqlManager.getExecution(sqlCreate);
			assertNotNull(execution);
			
			assertEquals("CREATE TABLE demographic(userid INTEGER AUTO_INCREMENT NOT NULL, " +
					"firstName VARCHAR(100) NULL, lastName VARCHAR(100) NULL, creation DATETIME NULL, " +
					"CONSTRAINT pk_demographic PRIMARY KEY (userid))", execution.getSql());

			// use a second table to add a column to the above table
			final ColumnDefinition columnDescription = (ColumnDefinition)tableActivity.getColumn("description");
			assertNotNull(columnDescription);

/*
			final SqlAlter sqlAlter = sqlManager.getAlter(tableDemographic);
			assertNotNull(sqlAlter);
	
			sqlAlter.add(columnDescription);
	
			assertEquals("ALTER TABLE demographic ADD description TEXT NULL", sqlAlter.toString());
	
			// use and existing column in the table to emulate modifying a table.
			final ColumnDefinition columnCreation = (ColumnDefinition)tableDemographic.getColumn("creation");
			assertNotNull(columnCreation);
	
			sqlAlter.add(columnCreation);
	
			assertEquals("ALTER TABLE demographic ADD description TEXT NULL, MODIFY creation DATETIME NULL", sqlAlter
					.toString());

			// try a table with foreign keys
			final Table tableUserActivity = m_schema.getTable("useractivity");
			assertNotNull(tableUserActivity);
	
			sqlCreate = sqlManager.getCreate(tableUserActivity);
			assertNotNull(sqlCreate);
	
			assertEquals("CREATE TABLE useractivity(userid INTEGER NOT NULL, activityid INTEGER NOT NULL, "
					+ "CONSTRAINT fk_useractivity_demographic FOREIGN KEY (userid) REFERENCES demographic (userid), "
					+ "CONSTRAINT fk_useractivity_activity FOREIGN KEY (activityid) REFERENCES activity (activityid))",
					execution.getSql());
*/
		}
		catch (SqlBuilderException e) {
			e.printStackTrace();
			assertFalse(e.toString(), true);
		}
		catch (SqlManagerException e) {
			e.printStackTrace();
			assertFalse(e.toString(), true);
		}
	}

	/**
	 * This method tests SQL Server specific SQL statements.
	 */
	public void testSqlServer() {
		final SqlManager sqlManager = new SqlManagerSqlServer();
		assertNotNull(sqlManager);

		// test creating a table
		final Table tableActivity = m_schema.getTable("activity");
		assertNotNull(tableActivity);

		try {
			SqlCreate sqlCreate = new SqlCreate(tableActivity);
			assertNotNull(sqlCreate);
			
			com.tippingpoint.sql.base.SqlExecution execution = sqlManager.getExecution(sqlCreate);
			assertNotNull(execution);
	
			assertEquals("CREATE TABLE activity(activityid INTEGER IDENTITY NOT NULL, title VARCHAR(200) NULL, "
					+ "description TEXT NULL, creation DATETIME NULL, lastmodified DATETIME NULL, "
					+ "CONSTRAINT pk_activity PRIMARY KEY (activityid))", execution.getSql());

			// test creating a table
			final Table tableDemographic = m_schema.getTable("demographic");
			assertNotNull(tableDemographic);
	
			sqlCreate = new SqlCreate(tableDemographic);
			assertNotNull(sqlCreate);

			execution = sqlManager.getExecution(sqlCreate);
			assertNotNull(execution);
			
			assertEquals("CREATE TABLE demographic(userid INTEGER IDENTITY NOT NULL, firstName VARCHAR(100) NULL, " +
					"lastName VARCHAR(100) NULL, creation DATETIME NULL, CONSTRAINT pk_demographic " +
					"PRIMARY KEY (userid))", execution.getSql());

			// use a second table to add a column to the above table
			final ColumnDefinition columnDescription = (ColumnDefinition)tableActivity.getColumn("description");
			assertNotNull(columnDescription);

/*		
			final SqlAlter sqlAlter = sqlManager.getAlter(tableDemographic);
			assertNotNull(sqlAlter);
	
			sqlAlter.add(columnDescription);
	
			assertEquals("ALTER TABLE demographic ADD description TEXT NULL", sqlAlter.toString());
	
			// use and existing column in the table to emulate modifying a table.
			final ColumnDefinition columnCreation = (ColumnDefinition)tableDemographic.getColumn("creation");
			assertNotNull(columnCreation);
	
			sqlAlter.add(columnCreation);
	
			assertEquals("ALTER TABLE demographic ADD description TEXT NULL, ALTER COLUMN creation DATETIME NULL", sqlAlter
					.toString());
	
			// try a table with foreign keys
			final Table tableUserActivity = m_schema.getTable("useractivity");
			assertNotNull(tableUserActivity);
	
			sqlCreate = sqlManager.getCreate(tableUserActivity);
			assertNotNull(sqlCreate);
	
			assertEquals("CREATE TABLE useractivity(userid INTEGER NOT NULL, activityid INTEGER NOT NULL, "
					+ "CONSTRAINT fk_useractivity_demographic FOREIGN KEY (userid) REFERENCES demographic (userid), "
					+ "CONSTRAINT fk_useractivity_activity FOREIGN KEY (activityid) REFERENCES activity (activityid))",
					execution.getSql());
*/
		}
		catch (SqlBuilderException e) {
			e.printStackTrace();
			assertFalse(e.toString(), true);
		}
		catch (SqlManagerException e) {
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
}

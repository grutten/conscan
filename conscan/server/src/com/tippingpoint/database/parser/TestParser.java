package com.tippingpoint.database.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnTypeDate;
import com.tippingpoint.database.ColumnTypeId;
import com.tippingpoint.database.ColumnTypeString;
import com.tippingpoint.database.ColumnTypeText;
import com.tippingpoint.database.DatabaseException;
import com.tippingpoint.database.PrimaryKeyConstraint;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.sql.SqlExecutionException;
import com.tippingpoint.sql.SqlManagerException;
import com.tippingpoint.test.TestDbCase;

/**
 * This class tests the parsing of a database configuration file.
 */
public class TestParser extends TestDbCase {
	private static final String[] DB =
		{"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                                ",
				"<schema name=\"" + UNIT_TEST_SCHEMA_NAME + "\">                    ",
				"  <table name=\"demographic\">                                     ",
				"    <column name=\"demographicid\" type=\"id\"/>                   ",
				"    <column name=\"firstName\" type=\"string\" length=\"100\"/>    ",
				"    <column name=\"lastName\" type=\"string\" length=\"100\"/>     ",
				"    <column name=\"creation\" type=\"date\"/>                      ",
				"    <constraint name=\"pk_demographic\" type=\"primary\">          ",
				"      <column name=\"demographicid\"/>                             ",
				"    </constraint>                                                  ",
				"  </table>                                                         ",
				"  <table name=\"role\">                                            ",
				"    <column name=\"roleid\" type=\"id\"/>                          ",
				"    <column name=\"name\" type=\"string\" length=\"200\"/>         ",
				"    <constraint name=\"pk_role\" type=\"primary\">                 ",
				"      <column name=\"roleid\"/>                                    ",
				"    </constraint>                                                  ",
				"  </table>                                                         ",
				"  <table name=\"tagtype\">                                         ",
				"    <column name=\"tagtypeid\" type=\"id\"/>                       ",
				"    <column name=\"text\" type=\"string\" length=\"200\"/>         ",
				"    <constraint name=\"pk_tagtype\" type=\"primary\">              ",
				"      <column name=\"tagtypeid\"/>                                 ",
				"    </constraint>                                                  ",
				"  </table>                                                         ",
				"  <table name=\"tag\">                                             ",
				"    <column name=\"tagid\" type=\"id\"/>                           ",
				"    <column name=\"tagtypeid\" type=\"idref\"/>                    ",
				"    <column name=\"text\" type=\"string\" length=\"200\"/>         ",
				"    <constraint name=\"pk_tag\" type=\"primary\">                  ",
				"      <column name=\"tagid\"/>                                     ",
				"    </constraint>                                                  ",
				"    <constraint name=\"fk_tag_tagtype\" type=\"foreign\">          ",
				"      <column name=\"tagtypeid\"/>                                 ",
				"      <table name=\"tagtype\">                                     ",
				"        <column name=\"tagtypeid\"/>                               ",
				"      </table>                                                     ",
				"    </constraint>                                                  ",
				"  </table>                                                         ",
				"  <table name=\"roletag\">                                         ",
				"    <column name=\"roleid\" type=\"idref\" required=\"true\"/>     ",
				"    <column name=\"tagid\" type=\"idref\" required=\"true\"/>      ",
				"    <constraint name=\"fk_roletag_role\" type=\"foreign\">         ",
				"      <column name=\"roleid\"/>                                    ",
				"      <table name=\"role\">                                        ",
				"        <column name=\"roleid\"/>                                  ",
				"      </table>                                                     ",
				"    </constraint>                                                  ",
				"    <constraint name=\"fk_roletag_tag\" type=\"foreign\">          ",
				"      <column name=\"tagid\"/>                                     ",
				"      <table name=\"tag\">                                         ",
				"        <column name=\"tagid\"/>                                   ",
				"      </table>                                                     ",
				"    </constraint>                                                  ",
				"  </table>                                                         ",
				"  <table name=\"activity\">                                        ",
				"    <column name=\"activityid\" type=\"id\"/>                      ",
				"    <column name=\"title\" type=\"string\" length=\"200\"/>        ",
				"    <column name=\"description\" type=\"text\"/>                   ",
				"    <column name=\"creation\" type=\"date\"/>                      ",
				"    <column name=\"lastmodified\" type=\"date\"/>                  ",
				"    <constraint name=\"pk_activity\" type=\"primary\">             ",
				"      <column name=\"activityid\"/>                                ",
				"    </constraint>                                                  ",
				"  </table>                                                         ",
				"</schema>                                                          "};

	private static final String[] DB_DATA =
		{"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                                ",
				"<data>                                                             ",
				"  <item name=\"demographic\">                                    ",
				"    <column name=\"username\">tester</column>                    ",
				"    <column name=\"password\">password</column>                  ",
				"    <column name=\"email\">test@system.com</column>              ",
				"    <column name=\"firstName\">Test</column>                     ",
				"    <column name=\"lastName\">Administration</column>            ",
				"  </item>                                                        ",
				"  <item name=\"tagtype\">                                        ",
				"    <column name=\"text\">Security</column>                      ",
				"  </item>                                                        ",
				"  <item name=\"tag\">                                            ",
				"    <column name=\"text\">Public</column>                        ",
				"    <column name=\"tagtypeid\" value=\"referenced\">             ",
				"      <table name=\"tagtype\">                                   ",
				"        <column name=\"text\">Security</column>                  ",
				"      </table>                                                   ",
				"    </column>                                                    ",
				"  </item>                                                        ",
				"  <item name=\"role\">                                           ",
				"    <column name=\"name\">Administrator</column>                 ",
				"  </item>                                                        ",
				"</data>                                                            "};

	/**
	 * This method tests the basics.
	 */
	public void testBasics() {
		final Reader reader = new StringReader(getString(DB));
		try {
			final Schema schema = Parser.parse(reader);
			assertNotNull(schema);

			assertEquals(UNIT_TEST_SCHEMA_NAME, schema.getName());

			final Table tableDemographic = schema.getTable("demographic");
			assertNotNull(tableDemographic);

			Column column = tableDemographic.getColumn("demographicid");
			assertNotNull(column);
			assertEquals(ColumnTypeId.class, column.getType().getClass());

			column = tableDemographic.getColumn("firstName");
			assertNotNull(column);
			assertEquals(ColumnTypeString.class, column.getType().getClass());

			column = tableDemographic.getColumn("lastName");
			assertNotNull(column);
			assertEquals(ColumnTypeString.class, column.getType().getClass());

			column = tableDemographic.getColumn("creation");
			assertNotNull(column);
			assertEquals(ColumnTypeDate.class, column.getType().getClass());

			PrimaryKeyConstraint primaryKey = tableDemographic.getPrimaryKey();
			assertNotNull(primaryKey);
			assertEquals("pk_demographic", primaryKey.getName());
			assertEquals(tableDemographic, primaryKey.getTable());
			assertEquals(tableDemographic.getColumn("demographicid"), primaryKey.getColumns().next());

			final Table tableActivity = schema.getTable("activity");
			assertNotNull(tableActivity);

			column = tableActivity.getColumn("activityid");
			assertNotNull(column);
			assertEquals(ColumnTypeId.class, column.getType().getClass());

			column = tableActivity.getColumn("title");
			assertNotNull(column);
			assertEquals(ColumnTypeString.class, column.getType().getClass());

			column = tableActivity.getColumn("description");
			assertNotNull(column);
			assertEquals(ColumnTypeText.class, column.getType().getClass());

			column = tableActivity.getColumn("creation");
			assertNotNull(column);
			assertEquals(ColumnTypeDate.class, column.getType().getClass());

			column = tableActivity.getColumn("lastmodified");
			assertNotNull(column);
			assertEquals(ColumnTypeDate.class, column.getType().getClass());

			primaryKey = tableActivity.getPrimaryKey();
			assertNotNull(primaryKey);
			assertEquals("pk_activity", primaryKey.getName());
			assertEquals(tableActivity, primaryKey.getTable());
			assertEquals(tableActivity.getColumn("activityid"), primaryKey.getColumns().next());
		}
		catch (final IOException e) {
			e.printStackTrace();
			fail("I/O Exception: " + e.getMessage());
		}
		catch (final SAXException e) {
			e.printStackTrace();
			fail("SAX Exception: " + e.getMessage());
		}
	}

	/**
	 * This method tests the parsing of the import data.
	 */
	public void testImport() {
		final Reader readerDb = new StringReader(getString(DB));
		final Reader readerData = new StringReader(getString(DB_DATA));

		try {
			final Schema schema = Parser.parse(readerDb);
			assertNotNull(schema);

			refreshDb(schema);

			Parser.parseImport(readerData, new Importer(schema));
		}
		catch (final IOException e) {
			e.printStackTrace();
			fail("I/O Exception: " + e.getMessage());
		}
		catch (final SAXException e) {
			e.printStackTrace();
			fail("SAX Exception: " + e.getMessage());
		}
		catch (final DatabaseException e) {
			e.printStackTrace();
			fail("Database Exception: " + e.getMessage());
		}
		catch (final SQLException e) {
			e.printStackTrace();
			fail("SQL Exception: " + e.getMessage());
		}
		catch (final SqlManagerException e) {
			e.printStackTrace();
			fail("SQL Builder Exception: " + e.getMessage());
		}
		catch (final SqlExecutionException e) {
			e.printStackTrace();
			fail("SQL Execution Exception: " + e.getMessage());
		}
		catch (final SqlBaseException e) {
			e.printStackTrace();
			fail("SQL Base Exception: " + e.getMessage());
		}
	}

	/**
	 * This method converts an array of strings to a single string.
	 */
	private String getString(final String[] astrValue) {
		final StringBuilder strBuffer = new StringBuilder();

		if (astrValue != null && astrValue.length > 0) {
			for (int nIndex = 0; nIndex < astrValue.length; ++nIndex) {
				strBuffer.append(StringUtils.trimToEmpty(astrValue[nIndex]));
			}
		}

		return strBuffer.toString();
	}
}

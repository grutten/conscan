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
		{"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
				"<Schema name=\"testparserschema\">",
				"  <Table name=\"demographic\">",
				"    <Column name=\"demographicid\" type=\"id\"/>",
				"    <Column name=\"firstName\" type=\"string\" length=\"100\"/>",
				"    <Column name=\"lastName\" type=\"string\" length=\"100\"/>",
				"    <Column name=\"creation\" type=\"date\"/>",
				"    <Constraint name=\"pk_demographic\" type=\"primary\">",
				"      <Column name=\"demographicid\"/>",
				"    </Constraint>",
				"  </Table>",
				"  <Table name=\"role\">",
				"    <Column name=\"roleid\" type=\"id\"/>",
				"    <Column name=\"name\" type=\"string\" length=\"200\"/>",
				"    <Constraint name=\"pk_role\" type=\"primary\">",
				"      <Column name=\"roleid\"/>",
				"    </Constraint>",
				"  </Table>",
				"  <Table name=\"tagtype\">",
				"    <Column name=\"tagtypeid\" type=\"id\"/>",
				"    <Column name=\"text\" type=\"string\" length=\"200\"/>",
				"    <Constraint name=\"pk_tagtype\" type=\"primary\">",
				"      <Column name=\"tagtypeid\"/>",
				"    </Constraint>",
				"  </Table>",
				"  <Table name=\"tag\">",
				"    <Column name=\"tagid\" type=\"id\"/>",
				"    <Column name=\"tagtypeid\" type=\"idref\"/>",
				"    <Column name=\"text\" type=\"string\" length=\"200\"/>",
				"    <Constraint name=\"pk_tag\" type=\"primary\">",
				"      <Column name=\"tagid\"/>",
				"    </Constraint>",
				"    <Constraint name=\"fk_tag_tagtype\" type=\"foreign\">",
				"      <Column name=\"tagtypeid\"/>",
				"      <Table name=\"tagtype\">",
				"        <Column name=\"tagtypeid\"/>",
				"      </Table>",
				"    </Constraint>",
				"  </Table>",
				"  <Table name=\"roletag\">",
				"    <Column name=\"roleid\" type=\"idref\" required=\"true\"/>",
				"    <Column name=\"tagid\" type=\"idref\" required=\"true\"/>",
				"    <Constraint name=\"fk_roletag_role\" type=\"foreign\">",
				"      <Column name=\"roleid\"/>",
				"      <Table name=\"role\">",
				"        <Column name=\"roleid\"/>",
				"      </Table>",
				"    </Constraint>",
				"    <Constraint name=\"fk_roletag_tag\" type=\"foreign\">",
				"      <Column name=\"tagid\"/>",
				"      <Table name=\"tag\">",
				"        <Column name=\"tagid\"/>",
				"      </Table>",
				"    </Constraint>",
				"  </Table>",
				"  <Table name=\"activity\">",
				"    <Column name=\"activityid\" type=\"id\"/>",
				"    <Column name=\"title\" type=\"string\" length=\"200\"/>",
				"    <Column name=\"description\" type=\"text\"/>",
				"    <Column name=\"creation\" type=\"date\"/>",
				"    <Column name=\"lastmodified\" type=\"date\"/>",
				"    <Constraint name=\"pk_activity\" type=\"primary\">",
				"      <Column name=\"activityid\"/>",
				"    </Constraint>",
				"  </Table>",
				"</Schema>"};

	private static final String[] DB_DATA =
		{"<data>", "  <Table name=\"demographic\">",
				"    <item>",
				"      <Column name=\"username\">tester</Column>",
				"      <Column name=\"password\">password</Column>",
				"      <Column name=\"email\">test@system.com</Column>",
				"      <Column name=\"firstName\">Test</Column>",
				"      <Column name=\"lastName\">Administration</Column>",
				"    </item>",
				"  </Table>",
				"  <Table name=\"tagtype\">",
				"    <item>",
				"      <Column name=\"text\">Security</Column>",
				"    </item>",
				"  </Table>",
				"  <Table name=\"tag\">",
				"    <item>",
				"      <Column name=\"text\">Public</Column>",
				"      <Column name=\"tagtypeid\" value=\"referenced\">",
				"        <Table name=\"tagtype\">",
				"          <Column name=\"text\">Security</Column>",
				"        </Table>",
				"      </Column>",
				"    </item>",
				"  </Table>",
				"  <Table name=\"role\">",
				"    <item>",
				"      <Column name=\"name\">Administrator</Column>",
				"    </item>",
				"  </Table>",
				"</data>"};

	/**
	 * This method tests the basics.
	 */
	public void testBasics() {
		final Reader reader = new StringReader(getString(DB));
		try {
			final Schema schema = Parser.parse(reader);
			assertNotNull(schema);

			assertEquals("testparserschema", schema.getName());

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

package com.tippingpoint.database;

import java.util.Iterator;
import com.tippingpoint.test.TestCommonCase;

/**
 * This class is used to exercise the table and related classes.
 */
public final class TestTable extends TestCommonCase {
	/**
	 * This method tests the basics.
	 */
	public void testBasics() {
		try {
			final String strSchemaName = "TestSchema";
			final Schema schema = new Schema(strSchemaName);
			assertNotNull(schema);
			assertEquals(strSchemaName, schema.getName());

			final String strTable = "TestTable";

			final Table table = new Table(schema, strTable);
			assertNotNull(table);
			assertEquals(strTable, table.getName());

			Column column = addColumn(table, "Email", 100, ColumnTypeFactory.getFactory().get(ColumnTypeString.TYPE));

			String strConstraintName = "PK_" + strTable;
			Constraint constraint = new PrimaryKeyConstraint();
			assertNotNull(constraint);

			constraint.setName(strConstraintName);
			table.add(constraint);
			assertEquals(constraint, table.getPrimaryKey());
			assertEquals(strConstraintName, constraint.getName());

			constraint.addColumn(column);
			assertEquals(column, constraint.getColumns().next());

			// add another column
			column = addColumn(table, "FirstName", 50, ColumnTypeFactory.getFactory().get(ColumnTypeString.TYPE));

			strConstraintName = "LK_" + strTable;
			constraint = new LogicalKeyConstraint();
			assertNotNull(constraint);

			constraint.setName(strConstraintName);
			table.add(constraint);

			assertEquals(constraint, table.getLogicalKey());
			assertEquals(strConstraintName, constraint.getName());

			constraint.addColumn(column);
			assertEquals(column, constraint.getColumns().next());

			column = addColumn(table, "LastName", 50, ColumnTypeFactory.getFactory().get(ColumnTypeString.TYPE));
			constraint.addColumn(column);

			final Iterator<Column> iterConstraints = constraint.getColumns();
			assertNotNull(iterConstraints);

			assertTrue(iterConstraints.hasNext());
			assertEquals(table.getColumn("FirstName"), iterConstraints.next());

			assertTrue(iterConstraints.hasNext());
			assertEquals(table.getColumn("LastName"), iterConstraints.next());

			assertFalse(iterConstraints.hasNext());
		}
		catch (final DatabaseElementException e) {
			e.printStackTrace(System.out);
		}
	}

	/**
	 * This method creates a named column for the table.
	 */
	private Column addColumn(final Table table, final String strName, final int nColumnLength, final ColumnType type) {
		assertNotNull(table);
		assertNotNull(strName);
		assertTrue(strName.length() > 0);
		assertNotNull(type);

		final ColumnDefinition column = new ColumnDefinition(table, strName, type);
		assertNotNull(column);
		assertEquals(table, column.getTable());
		assertEquals(strName, column.getName());
		assertEquals(type, column.getType());

		// if this is a string, set the length
		if (type.hasLength()) {
			column.setLength(nColumnLength);
			assertEquals(nColumnLength, column.getLength());
		}

		assertNull(column.getDefault());
		assertFalse(column.isRequired());

		// make sure the table thinks the column exists
		assertEquals(column, table.getColumn(strName));

		return column;
	}
}

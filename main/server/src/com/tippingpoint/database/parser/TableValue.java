package com.tippingpoint.database.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.tippingpoint.database.Table;

/**
 * This class is use track a referenced table.
 */
public class TableValue {
	/** This member holds the list of column values. */
	private final List<ColumnValue> m_listColumnValues = new ArrayList<ColumnValue>();

	/** This member holds the referenced table. */
	private final Table m_table;

	/**
	 * This method constructs a new value for the specified table.
	 */
	public TableValue(final Table table) {
		m_table = table;
	}

	/**
	 * This method adds a column value to the table value.
	 * 
	 * @param columnValue ColumnValue containing a piece to identify a row in the referenced table.
	 */
	public void add(final ColumnValue columnValue) {
		m_listColumnValues.add(columnValue);
	}

	/**
	 * This method returns an iterator over the column values.
	 */
	public Iterator<ColumnValue> getColumnValues() {
		return m_listColumnValues.iterator();
	}

	/**
	 * This method returns the table being referenced.
	 */
	public Table getTable() {
		return m_table;
	}
}

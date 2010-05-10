package com.tippingpoint.sql;

import java.util.ArrayList;
import java.util.List;
import com.tippingpoint.database.Table;

/**
 * TableBuilderCommand This class is the base class for single table specific commands (create, drop, alter, etc.).
 */
public abstract class TableBuilderCommand extends BuilderCommand {
	/** This member holds the table to be created. */
	protected Table m_table;

	/** This member holds the columns and their values to be inserted. */
	private List<ParameterizedValue> m_listColumns = new ArrayList<ParameterizedValue>();

	/**
	 * This method constructs a new statement for the given table.
	 */
	public TableBuilderCommand(final Table table) {
		m_table = table;
	}

	/**
	 * This method adds a parameterized value to the insert statement.
	 */
	public void add(final ParameterizedValue value) {
		m_listColumns.add(value);
	}

	public List<ParameterizedValue> getColumns() {
		return m_listColumns;
	}

	/**
	 * This method returns the table associated with the command.
	 */
	public Table getTable() {
		return m_table;
	}
}

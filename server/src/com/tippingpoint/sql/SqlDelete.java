package com.tippingpoint.sql;

import com.tippingpoint.database.Table;

/**
 * This class defines the methods needed to create a SQL delete statement.
 */
public final class SqlDelete extends TableBuilderCommand {
	/**
	 * This method constructs a new delete statement for the given table.
	 */
	public SqlDelete(final Table table) {
		super(table);
	}

	/**
	 * This method returns the string representation of the SQL command.
	 */
	@Override
	public String toString() {
		return "Delete " + m_table;
	}
}

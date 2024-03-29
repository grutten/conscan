package com.tippingpoint.sql;

import com.tippingpoint.database.Table;

/**
 * This class defines the methods needed to create a SQL update statement.
 */
public final class SqlUpdate extends TableBuilderCommand {
	/**
	 * This method constructs a new update statement for the given table.
	 */
	public SqlUpdate(final Table table) {
		super(table);
	}

	/**
	 * This method returns the string representation of the SQL command.
	 */
	@Override
	public String toString() {
		return "Update " + m_table;
	}
}

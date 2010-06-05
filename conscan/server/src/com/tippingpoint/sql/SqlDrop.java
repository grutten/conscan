package com.tippingpoint.sql;

import com.tippingpoint.database.Table;

/**
 * This class is use to generate the command to drop a table.
 */
public final class SqlDrop extends TableBuilderCommand {
	/**
	 * This method builds a drop table instance for the given table.
	 * 
	 * @throws SqlBuilderException
	 */
	public SqlDrop(final Table table) throws SqlBuilderException {
		super(table);

		// make sure there is a table specified
		if (m_table == null) {
			throw new SqlBuilderException("Table needs to be specified when droping tables.");
		}
	}

	/**
	 * This method returns the string representation of the SQL command.
	 */
	@Override
	public String toString() {
		return "DROP " + m_table;
	}
}

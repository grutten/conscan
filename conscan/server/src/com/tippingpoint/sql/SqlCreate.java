package com.tippingpoint.sql;

import com.tippingpoint.database.Table;

/**
 * SqlCreate This class generates the SQL used to generate a table create SQL statement.
 */
public class SqlCreate extends TableBuilderCommand {
	/**
	 * This method constructs a new statement for the given table.
	 * 
	 * @throws SqlBuilderException
	 */
	public SqlCreate(final Table table) throws SqlBuilderException {
		super(table);

		if (m_table == null) {
			throw new SqlBuilderException("Table instances needs to be specified when creating tables.");
		}
	}
	
	/**
	 * This method returns the string representation of the SQL command.
	 */
	public String toString() {
		return "Create " + m_table;
	}
}

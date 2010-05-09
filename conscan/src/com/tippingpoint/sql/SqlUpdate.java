package com.tippingpoint.sql;

import com.tippingpoint.database.Table;

/**
 * This class defines the methods needed to create a SQL update statement.
 */
public final class SqlUpdate extends TableBuilderCommand {
	/**
	 * This method constructs a new update statement for the given table.
	 */
	public SqlUpdate(Table table) {
		super(table);
	}
}

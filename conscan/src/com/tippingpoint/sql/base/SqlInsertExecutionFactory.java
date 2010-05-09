package com.tippingpoint.sql.base;

import com.tippingpoint.sql.Command;
import com.tippingpoint.sql.SqlInsert;

/**
 * This class generates SQL execution instances for SQL insert statements.
 */
public class SqlInsertExecutionFactory extends SqlExecutionFactory {
	/**
	 * This method returns an execution instance for the given command.
	 * @param sqlCommand Command to be executed.
	 */
	public SqlExecution getExecution(SqlManager sqlManager, Command sqlCommand) {
		if (!(sqlCommand instanceof SqlInsert)) {
			throw new IllegalArgumentException(getClass().getSimpleName() + " does not handle commands of type " + sqlCommand.getClass());
		}
		
		return new SqlInsertExecution(sqlManager, (SqlInsert)sqlCommand);
	}
}

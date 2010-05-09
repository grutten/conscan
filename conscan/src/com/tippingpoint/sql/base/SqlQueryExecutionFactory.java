package com.tippingpoint.sql.base;

import com.tippingpoint.sql.Command;
import com.tippingpoint.sql.SqlQuery;

/**
 * This class generates SQL execution instances for SQL query statements.
 */
public class SqlQueryExecutionFactory extends SqlExecutionFactory {
	/**
	 * This method returns an execution instance for the given command.
	 * @param sqlCommand Command to be executed.
	 */
	public SqlExecution getExecution(SqlManager sqlManager, Command sqlCommand) {
		if (!(sqlCommand instanceof SqlQuery)) {
			throw new IllegalArgumentException(getClass().getSimpleName() + " does not handle commands of type " + sqlCommand.getClass());
		}
		
		return new SqlQueryExecution(sqlManager, (SqlQuery)sqlCommand);
	}
}

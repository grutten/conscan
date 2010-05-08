package com.tippingpoint.sql.base;

import com.tippingpoint.sql.Command;
import com.tippingpoint.sql.SqlCreate;
import com.tippingpoint.sql.base.SqlExecution;

/**
 * This class generates SQL execution instances for SQL create statements.
 */
public class SqlExecutionCreateFactory extends SqlExecutionFactory {
	/**
	 * This method returns an execution instance for the given command.
	 * @param sqlCommand Command to be executed.
	 */
	public SqlExecution getExecution(SqlManager sqlManager, Command sqlCommand) {
		if (!(sqlCommand instanceof SqlCreate)) {
			throw new IllegalArgumentException(getClass().getSimpleName() + " does not handle commands of type " + sqlCommand.getClass());
		}
		
		return new SqlCreateExecution(sqlManager, (SqlCreate)sqlCommand);
	}
}

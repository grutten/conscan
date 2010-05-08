package com.tippingpoint.sql.base;

import com.tippingpoint.sql.Command;
import com.tippingpoint.sql.SqlDrop;

/**
 * This class generates SQL execution instances for SQL drop statements.
 */
public class SqlExecutionDropFactory extends SqlExecutionFactory {
	/**
	 * This method returns an execution instance for the given command.
	 * @param sqlCommand Command to be executed.
	 */
	public SqlExecution getExecution(SqlManager sqlManager, Command sqlCommand) {
		if (!(sqlCommand instanceof SqlDrop)) {
			throw new IllegalArgumentException(getClass().getSimpleName() + " does not handle commands of type " + sqlCommand.getClass());
		}
		
		return new SqlDropExecution(sqlManager, (SqlDrop)sqlCommand);
	}
}

package com.tippingpoint.sql.base;

import com.tippingpoint.sql.Command;
import com.tippingpoint.sql.SqlCreate;

/**
 * This class generates SQL execution instances for SQL create statements.
 */
public class SqlCreateExecutionFactory extends SqlExecutionFactory {
	/**
	 * This method returns an execution instance for the given command.
	 * 
	 * @param sqlCommand Command to be executed.
	 */
	@Override
	public SqlExecution getExecution(final SqlManager sqlManager, final Command sqlCommand) {
		if (!(sqlCommand instanceof SqlCreate)) {
			throw new IllegalArgumentException(getClass().getSimpleName() + " does not handle commands of type " +
					sqlCommand.getClass());
		}

		return new SqlCreateExecution(sqlManager, (SqlCreate)sqlCommand);
	}
}

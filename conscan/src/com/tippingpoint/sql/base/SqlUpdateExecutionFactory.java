package com.tippingpoint.sql.base;

import com.tippingpoint.sql.Command;
import com.tippingpoint.sql.SqlUpdate;

/**
 * This class generates SQL execution instances for SQL update statements.
 */
public class SqlUpdateExecutionFactory extends SqlExecutionFactory {
	/**
	 * This method returns an execution instance for the given command.
	 * 
	 * @param sqlCommand Command to be executed.
	 */
	@Override
	public SqlExecution getExecution(final SqlManager sqlManager, final Command sqlCommand) {
		if (!(sqlCommand instanceof SqlUpdate)) {
			throw new IllegalArgumentException(getClass().getSimpleName() + " does not handle commands of type " +
					sqlCommand.getClass());
		}

		return new SqlUpdateExecution(sqlManager, (SqlUpdate)sqlCommand);
	}
}

package com.tippingpoint.sql.mysql;

import com.tippingpoint.sql.Command;
import com.tippingpoint.sql.SqlAlter;
import com.tippingpoint.sql.base.SqlExecution;
import com.tippingpoint.sql.base.SqlExecutionFactory;
import com.tippingpoint.sql.base.SqlManager;

/**
 * This class generates SQL execution instances for SQL alter statements.
 */
public class SqlAlterExecutionFactory extends SqlExecutionFactory {
	/**
	 * This method returns an execution instance for the given command.
	 * 
	 * @param sqlCommand Command to be executed.
	 */
	@Override
	public SqlExecution getExecution(final SqlManager sqlManager, final Command sqlCommand) {
		if (!(sqlCommand instanceof SqlAlter)) {
			throw new IllegalArgumentException(getClass().getSimpleName() + " does not handle commands of type " +
					sqlCommand.getClass());
		}

		return new SqlAlterExecution(sqlManager, (SqlAlter)sqlCommand);
	}
}

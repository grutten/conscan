package com.tippingpoint.sql.base;

import com.tippingpoint.sql.Command;
import com.tippingpoint.sql.base.SqlExecution;

/**
 * This class is a base class for generating classes to execution a SQL command.
 */
public abstract class SqlExecutionFactory {
	/**
	 * This method returns an execution instance for the given command.
	 * @param sqlManager SqlManager that is generating the execution
	 * @param sqlCommand Command to be executed.
	 */
	public abstract SqlExecution getExecution(SqlManager sqlManager, Command sqlCommand);
}

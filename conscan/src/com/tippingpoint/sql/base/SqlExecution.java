package com.tippingpoint.sql.base;

import com.tippingpoint.sql.SqlBuilderException;

/**
 * This class is a base class used to generate and execute SQL statements.
 */
public abstract class SqlExecution {
	/** This member holds the manager used to create the execution. */
	protected SqlManager m_sqlManager;

	/**
	 * This method constructs a new execution for the given manager.
	 */
	public SqlExecution(SqlManager sqlManager) {
		m_sqlManager = sqlManager;
	}

	/**
	 * This method is used to generated the SQL statement.
	 * @throws SqlBuilderException 
	 */
	public abstract String getSql() throws SqlBuilderException;
}

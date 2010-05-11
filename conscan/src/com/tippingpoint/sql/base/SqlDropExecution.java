package com.tippingpoint.sql.base;

import com.tippingpoint.database.Table;
import com.tippingpoint.sql.SqlBuilderException;
import com.tippingpoint.sql.SqlDrop;

/**
 * This class is used to execute the SQL drop.
 */
public class SqlDropExecution extends SqlExecution {
	/** This member holds the source of the command. */
	private final SqlDrop m_sqlDrop;

	/**
	 * This method constructs a new execution for the given manager.
	 */
	public SqlDropExecution(final SqlManager sqlManager, final SqlDrop sqlDrop) {
		super(sqlManager);

		m_sqlDrop = sqlDrop;
	}

	/**
	 * This method is used to generated the SQL statement.
	 * 
	 * @throws SqlBuilderException
	 */
	@Override
	public String getSql() throws SqlBuilderException {
		final Table table = m_sqlDrop.getTable();

		final StringBuilder strSql = new StringBuilder();

		// the statement starts with the create statement
		strSql.append("DROP TABLE ");
		strSql.append(table.getName());

		return strSql.toString();
	}
}

package com.tippingpoint.sql.base;

import com.tippingpoint.database.Table;
import com.tippingpoint.sql.SqlBuilderException;
import com.tippingpoint.sql.SqlDelete;

/**
 * This class is used to execute the SQL delete.
 */
public class SqlDeleteExecution extends SqlExecution {
	/** This member holds the source of the command. */
	private final SqlDelete m_sqlDelete;

	/**
	 * This method constructs a new execution for the given manager.
	 */
	public SqlDeleteExecution(final SqlManager sqlManager, final SqlDelete sqlDelete) {
		super(sqlManager);

		m_sqlDelete = sqlDelete;
	}

	/**
	 * This method is used to generated the SQL statement.
	 * 
	 * @throws SqlBuilderException
	 */
	@Override
	protected String generateSql() throws SqlBuilderException {
		final Table table = m_sqlDelete.getTable();

		final StringBuilder strSql = new StringBuilder();

		strSql.append("DELETE FROM ");
		strSql.append(table);

		addWheres(m_sqlDelete.getWheres(), strSql);

		return strSql.toString();
	}
}

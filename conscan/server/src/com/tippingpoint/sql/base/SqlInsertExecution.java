package com.tippingpoint.sql.base;

import java.util.Iterator;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.ParameterizedValue;
import com.tippingpoint.sql.SqlBuilderException;
import com.tippingpoint.sql.SqlInsert;

/**
 * This class is used to execute the SQL insert.
 */
public class SqlInsertExecution extends SqlExecution {
	/** This member holds the source of the command. */
	private final SqlInsert m_sqlInsert;

	/**
	 * This method constructs a new execution for the given manager.
	 */
	public SqlInsertExecution(final SqlManager sqlManager, final SqlInsert sqlInsert) {
		super(sqlManager);

		m_sqlInsert = sqlInsert;
	}

	/**
	 * This method is used to generated the SQL statement.
	 * 
	 * @throws SqlBuilderException
	 */
	@Override
	protected String generateSql() throws SqlBuilderException {
		final Table table = m_sqlInsert.getTable();

		final StringBuilder strSql = new StringBuilder();

		strSql.append("INSERT INTO ");
		strSql.append(table);
		strSql.append("(");

		final StringBuilder strValues = new StringBuilder("VALUES(");

		final Iterator<ParameterizedValue> iterColumns = m_sqlInsert.getColumns().iterator();
		while (iterColumns.hasNext()) {
			final ParameterizedValue value = iterColumns.next();

			strSql.append(value.getColumn().getName());
			strValues.append("?");
			add(value);

			if (iterColumns.hasNext()) {
				strSql.append(", ");
				strValues.append(", ");
			}
		}

		strSql.append(") ");
		strValues.append(")");

		strSql.append(strValues);

		return strSql.toString();
	}
}

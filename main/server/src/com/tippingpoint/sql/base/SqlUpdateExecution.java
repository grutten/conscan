package com.tippingpoint.sql.base;

import java.util.Iterator;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.ParameterizedValue;
import com.tippingpoint.sql.SqlBuilderException;
import com.tippingpoint.sql.SqlUpdate;

/**
 * This class is used to execute the SQL update.
 */
public class SqlUpdateExecution extends SqlExecution {
	/** This member holds the source of the command. */
	private final SqlUpdate m_sqlUpdate;

	/**
	 * This method constructs a new execution for the given manager.
	 */
	public SqlUpdateExecution(final SqlManager sqlManager, final SqlUpdate sqlUpdate) {
		super(sqlManager);

		m_sqlUpdate = sqlUpdate;
	}

	/**
	 * This method is used to generated the SQL statement.
	 * 
	 * @throws SqlBuilderException
	 */
	@Override
	protected String generateSql() throws SqlBuilderException {
		final Table table = m_sqlUpdate.getTable();

		final StringBuilder strSql = new StringBuilder();

		strSql.append("UPDATE ");
		strSql.append(table);
		strSql.append(" SET ");

		final Iterator<ParameterizedValue> iterColumns = m_sqlUpdate.getColumns().iterator();
		while (iterColumns.hasNext()) {
			final ParameterizedValue value = iterColumns.next();

			strSql.append(value.getColumn().getName());
			strSql.append(" = ?");
			add(value);

			if (iterColumns.hasNext()) {
				strSql.append(", ");
			}
		}

		addWheres(m_sqlUpdate.getWheres(), strSql);

		return strSql.toString();
	}
}

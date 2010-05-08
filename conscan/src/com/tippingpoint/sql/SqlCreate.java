package com.tippingpoint.sql;

import java.util.Iterator;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.Table;

/**
 * SqlCreate This class generates the SQL used to generate a table create SQL statement.
 */
public class SqlCreate extends TableBuilderCommand {
	/**
	 * This method constructs a new statement for the given table.
	 * 
	 * @throws SqlBuilderException
	 */
	public SqlCreate(final Table table) throws SqlBuilderException {
		super(table);

		if (m_table == null) {
			throw new SqlBuilderException("Table instances needs to be specified when creating tables.");
		}
	}

	/**
	 * This method creates the statement used to generate the table.
	 * 
	 * @throws SqlBuilderException
	 */
	@Override
	protected SqlExecution createExecution() throws SqlBuilderException {
		// make sure there is a table specified
		if (m_table == null) {
			throw new SqlBuilderException("Table needs to be specified when creating tables.");
		}

		final StringBuilder strSql = new StringBuilder();

		// the statement starts with the create statement
		strSql.append("CREATE TABLE ");
		strSql.append(m_table.getName());
		strSql.append('(');

		// add in the column statements next
		final Iterator<Column> iterTableColumns = m_table.getColumns();
		while (iterTableColumns.hasNext()) {
			final ColumnDefinition column = (ColumnDefinition)iterTableColumns.next();
			strSql.append(getPhrase(column));

			if (iterTableColumns.hasNext()) {
				strSql.append(", ");
			}
		}

		// add the various constraints to the table
		Constraint constraint = m_table.getPrimaryKey();
		if (constraint != null) {
			strSql.append(", ");
			strSql.append(getPhrase(constraint));
		}

		constraint = m_table.getLogicalKey();
		if (constraint != null) {
			strSql.append(", ");
			strSql.append(getPhrase(constraint));
		}

		final Iterator<Constraint> iterConstraints = m_table.getConstraints();
		if (iterConstraints != null) {
			while (iterConstraints.hasNext()) {
				strSql.append(", ");
				strSql.append(getPhrase(iterConstraints.next()));
			}
		}

		strSql.append(')');

		return new SqlExecution(m_builder.getConverter(), strSql.toString());
	}
}

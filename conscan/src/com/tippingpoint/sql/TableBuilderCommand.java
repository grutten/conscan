package com.tippingpoint.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.ForeignKey;
import com.tippingpoint.database.ForeignKeyConstraint;
import com.tippingpoint.database.Index;
import com.tippingpoint.database.LogicalKeyConstraint;
import com.tippingpoint.database.PrimaryKeyConstraint;
import com.tippingpoint.database.Table;

/**
 * TableBuilderCommand This class is the base class for single table specific commands (create, drop, alter, etc.).
 */
public abstract class TableBuilderCommand extends BuilderCommand {
	/** This member holds the columns and their values to be inserted. */
	private List<ParameterizedValue> m_listColumns = new ArrayList<ParameterizedValue>();

	/** This member holds the table to be created. */
	protected Table m_table;

	/**
	 * This method constructs a new statement for the given table.
	 */
	public TableBuilderCommand(final Table table) {
		m_table = table;
	}

	/**
	 * This method adds a parameterized value to the insert statement.
	 */
	public void add(final ParameterizedValue value) {
		m_listColumns.add(value);
	}

	/**
	 * This method returns the table associated with the command.
	 */
	public Table getTable() {
		return m_table;
	}

	/**
	 * This method returns the text for a single column for inclusion in a SQL statement.
	 * 
	 * @param column Column instance for which to create the phrase.
	 * @throws SqlBuilderException
	 */
	protected String getPhrase(final ColumnDefinition column) throws SqlBuilderException {
		final StringBuilder strSql = new StringBuilder();
		strSql.append(column.getName());
		strSql.append(' ');

		final String strType = m_builder.getType(column);
		if (StringUtils.isBlank(strType)) {
			throw new SqlBuilderException("Column type of '" + column.getType().getType() +
					"' not recognized for this database.");
		}

		strSql.append(strType);

		if (column.isRequired()) {
			strSql.append(" NOT");
		}

		strSql.append(" NULL");

		if (column.getDefault() != null) {
			strSql.append(' ').append(m_builder.getKeyword(SqlBuilder.KEYWORD_COLUMN_DEFAULT)).append(' ');
			strSql.append(m_builder.getConverter().convertToSqlString(column.getType(), column.getDefault()));
		}
		// else if (!column.isRequired()) // only default to null if it is nullable
		// strSql.append(" DEFAULT NULL");

		return strSql.toString();
	}

	/**
	 * This method returns the text for a constraint for inclusion in a SQL statement.
	 * 
	 * @param constraint Constraint instance for which to create the phrase.
	 */
	protected String getPhrase(final Constraint constraint) {
		final StringBuilder strSql = new StringBuilder();

		String strType = null;
		if (constraint instanceof PrimaryKeyConstraint) {
			strType = "PRIMARY KEY";
		}
		else if (constraint instanceof LogicalKeyConstraint) {
			strType = "UNIQUE";
		}
		else if (constraint instanceof ForeignKeyConstraint) {
			strType = "FOREIGN KEY";
		}
		else if (constraint instanceof Index) {
			if (((Index)constraint).isUnique()) {
				strType = "UNIQUE";
			}
		}

		if (strType != null) {
			strSql.append("CONSTRAINT ");

			strSql.append(constraint.getName());
			strSql.append(' ');
			strSql.append(strType);
			strSql.append(" (");

			Iterator<Column> iterConstraintColumns = constraint.getColumns();
			while (iterConstraintColumns.hasNext()) {
				final Column column = iterConstraintColumns.next();
				strSql.append(column.getName());

				if (iterConstraintColumns.hasNext()) {
					strSql.append(", ");
				}
			}

			strSql.append(")");

			// if this is a foreign key, the parent table must be referenced
			if (constraint instanceof ForeignKeyConstraint) {
				final ForeignKeyConstraint keyConstraint = (ForeignKeyConstraint)constraint;

				strSql.append(" REFERENCES ");
				strSql.append(keyConstraint.getForeignTable().getName());
				strSql.append(" (");

				iterConstraintColumns = constraint.getColumns();
				while (iterConstraintColumns.hasNext()) {
					final ForeignKey key = (ForeignKey)iterConstraintColumns.next();
					strSql.append(key.getParentColumn().getName());

					if (iterConstraintColumns.hasNext()) {
						strSql.append(", ");
					}
				}

				strSql.append(")");
			}
		}

		return strSql.toString();
	}

	public void setColumns(List<ParameterizedValue> m_listColumns) {
		this.m_listColumns = m_listColumns;
	}

	public List<ParameterizedValue> getColumns() {
		return m_listColumns;
	}
}

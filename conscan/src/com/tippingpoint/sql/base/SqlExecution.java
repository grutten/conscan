package com.tippingpoint.sql.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.DatabaseException;
import com.tippingpoint.database.ForeignKey;
import com.tippingpoint.database.ForeignKeyConstraint;
import com.tippingpoint.database.Index;
import com.tippingpoint.database.LogicalKeyConstraint;
import com.tippingpoint.database.PrimaryKeyConstraint;
import com.tippingpoint.sql.Condition;
import com.tippingpoint.sql.ParameterizedValue;
import com.tippingpoint.sql.SqlBuilderException;

/**
 * This class is a base class used to generate and execute SQL statements.
 */
public abstract class SqlExecution {
	/** This member holds the manager used to create the execution. */
	protected SqlManager m_sqlManager;

	/** This member holds the map of the columns and their values. */
	private final List<ParameterizedValue> m_listParameters = new ArrayList<ParameterizedValue>();

	/** This member holds the mapping of the columns to the index of the column in the SQL statement. */
	private final Map<Column, Integer> m_mapColumns = new HashMap<Column, Integer>();

	/**
	 * This method constructs a new execution for the given manager.
	 */
	public SqlExecution(final SqlManager sqlManager) {
		m_sqlManager = sqlManager;
	}

	/**
	 * This method adds a parameterized value to the collection.
	 */
	public void add(final ParameterizedValue value) {
		m_listParameters.add(value);
	}

	/**
	 * This method returns the index of the column from the statement.
	 */
	public Integer getColumnIndex(final Column column) {
		return m_mapColumns.get(column);
	}

	/**
	 * This method returns an iterator for the column map
	 */
	public Iterator<Map.Entry<Column, Integer>> getColumnMap() {
		return m_mapColumns.entrySet().iterator();
	}

	/**
	 * This method returns an object of the type indicated by the column.
	 * 
	 * @param column Column found in the query for which the object is to be returned.
	 * @param rs ResultSet containing the DB results.
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	public Object getObject(final Column column, final ResultSet rs) throws SQLException, DatabaseException {
		Object objValue = null;

		final Integer intIndex = m_mapColumns.get(column);
		if (intIndex != null) {
			objValue = m_sqlManager.getConverter().getObject(column.getType(), rs, intIndex);
		}

		return objValue;
	}

	/**
	 * This method is used to generated the SQL statement.
	 * 
	 * @throws SqlBuilderException
	 */
	public abstract String getSql() throws SqlBuilderException;

	/**
	 * This method adds a column to the column map. It is assumed that the SQL exists that corresponds to this mapping.
	 */
	protected void addColumnMap(final Column column, final Integer intIndex) {
		m_mapColumns.put(column, intIndex);
	}

	/**
	 * This method is used to add the where clauses to the SQL statement.
	 * @param listWheres List of conditions to add to the SQL statement.
	 * @param strSql StringBuilder containing the SQL statement to modify.
	 */
	protected void addWheres(List<Condition> listWheres, StringBuilder strSql) {
		if (listWheres != null && !listWheres.isEmpty()) {
			strSql.append(" WHERE ");

			Iterator<Condition> iterWheres = listWheres.iterator();
			while (iterWheres.hasNext()) {
				Condition condition = iterWheres.next();

				strSql.append(condition);

				if (condition.hasParameter()) {
					add(condition.getParameterValue());
				}

				if (iterWheres.hasNext()) {
					strSql.append(" AND ");
				}
			}
		}
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

		final String strType = m_sqlManager.getType(column);
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
			strSql.append(' ').append(m_sqlManager.getKeyword(SqlManager.KEYWORD_COLUMN_DEFAULT)).append(' ');
			strSql.append(m_sqlManager.getConverter().convertToSqlString(column.getType(), column.getDefault()));
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
}

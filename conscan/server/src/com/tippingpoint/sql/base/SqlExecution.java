package com.tippingpoint.sql.base;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.DatabaseException;
import com.tippingpoint.database.ForeignKey;
import com.tippingpoint.database.ForeignKeyConstraint;
import com.tippingpoint.database.Index;
import com.tippingpoint.database.LogicalKeyConstraint;
import com.tippingpoint.database.PrimaryKeyConstraint;
import com.tippingpoint.database.parser.ColumnValue;
import com.tippingpoint.sql.Condition;
import com.tippingpoint.sql.ParameterizedValue;
import com.tippingpoint.sql.SqlBuilderException;
import com.tippingpoint.sql.SqlExecutionException;

/**
 * This class is a base class used to generate and execute SQL statements.
 */
public abstract class SqlExecution {
	private static Log m_log = LogFactory.getLog(SqlExecution.class);

	/** This member holds the manager used to create the execution. */
	protected SqlManager m_sqlManager;

	/** This member holds the statement. */
	protected Statement m_stmt;

	/** This member holds the map of the columns and their values. */
	private final List<ParameterizedValue> m_listParameters = new ArrayList<ParameterizedValue>();

	/** This member holds the mapping of the columns to the index of the column in the SQL statement. */
	private final List<Column> m_mapColumns = new ArrayList<Column>();

	/** This member holds the string representing the SQL statement. */
	private String m_strSql;

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
	 * This method closes the SQL statement if opened.
	 */
	public void close() {
		if (m_stmt != null) {
			try {
				m_stmt.close();
			}
			catch (final SQLException e) {
				m_log.error("Error closing statement", e);
			}
			m_stmt = null;
		}
	}

	/**
	 * This method executes the statement.
	 * 
	 * @throws SqlBuilderException
	 * @throws SqlExecutionException
	 */
	public ResultSet executeQuery(final Connection conn) throws SqlBuilderException, SqlExecutionException {
		if (conn == null) {
			throw new IllegalArgumentException("Connection must be specified.");
		}

		ResultSet rs = null;
		final String strSql = getSql();

		try {
			if (m_listParameters != null && !m_listParameters.isEmpty()) {
				final PreparedStatement pstmt = getPreparedStatement(conn, strSql);

				rs = pstmt.executeQuery();
			}
			else {
				final Statement stmt = getStatement(conn);
				rs = stmt.executeQuery(strSql);
			}
		}
		catch (final SQLException e) {
			throw new SqlExecutionException(strSql, e);
		}

		return rs;
	}

	/**
	 * This method executes the statement.
	 * 
	 * @throws SqlExecutionException
	 * @throws SqlBuilderException
	 */
	public int executeUpdate(final Connection conn) throws SqlExecutionException, SqlBuilderException {
		int nRowsUpdated = 0;
		final String strSql = getSql();

		try {
			if (m_listParameters != null && !m_listParameters.isEmpty()) {
				final PreparedStatement pstmt = getPreparedStatement(conn, strSql);

				nRowsUpdated = pstmt.executeUpdate();
			}
			else {
				final Statement stmt = getStatement(conn);

				nRowsUpdated = stmt.executeUpdate(strSql);
			}
		}
		catch (final SQLException e) {
			throw new SqlExecutionException(strSql, e);
		}

		return nRowsUpdated;
	}

	/**
	 * This method returns the index of the column from the statement.
	 */
	public Integer getColumnIndex(final Column column) {
		return m_mapColumns.indexOf(column) + 1;
	}

	/**
	 * This method returns an iterator for the column map
	 */
	public Iterator<Column> getColumnMap() {
		return m_mapColumns.iterator();
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

		final Integer intIndex = getColumnIndex(column);
		if (intIndex != null) {
			objValue = m_sqlManager.getConverter().getObject(column.getType(), rs, intIndex);
		}

		return objValue;
	}

	/**
	 * This method returns an iterator over the parameterized values.
	 */
	public Iterator<ParameterizedValue> getParameters() {
		return m_listParameters.iterator();
	}

	/**
	 * This method returns the SQL statement.
	 * 
	 * @throws SqlBuilderException
	 */
	public final String getSql() throws SqlBuilderException {
		if (m_strSql == null) {
			m_strSql = generateSql();
		}

		return m_strSql;
	}

	/**
	 * This method adds a column to the column map. It is assumed that the SQL exists that corresponds to this mapping.
	 */
	protected void addColumnMap(final Column column) {
		m_mapColumns.add(column);
	}

	/**
	 * This method is used to add the where clauses to the SQL statement.
	 * 
	 * @param listWheres List of conditions to add to the SQL statement.
	 * @param strSql StringBuilder containing the SQL statement to modify.
	 */
	protected void addWheres(final List<Condition> listWheres, final StringBuilder strSql) {
		if (listWheres != null && !listWheres.isEmpty()) {
			strSql.append(" WHERE ");

			final Iterator<Condition> iterWheres = listWheres.iterator();
			while (iterWheres.hasNext()) {
				final Condition condition = iterWheres.next();

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
	 * This method is used to generated the SQL statement.
	 * 
	 * @throws SqlBuilderException
	 */
	protected abstract String generateSql() throws SqlBuilderException;

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

	/**
	 * This method returns a prepared statement. If the statement has not been generated, it will be and be returned.
	 * 
	 * @throws SQLException
	 */
	protected PreparedStatement getPreparedStatement(final Connection conn, final String strSql) throws SQLException {
		if (m_stmt == null) {
			m_stmt = conn.prepareStatement(strSql);
		}

		for (int nIndex = 0; nIndex < m_listParameters.size(); ++nIndex) {
			final ParameterizedValue value = m_listParameters.get(nIndex);
			final Object objValue =
				this.m_sqlManager.getConverter().convertToSqlObject(value.getColumn().getType(), value.getValue());
			if (objValue != null) {
				((PreparedStatement)m_stmt).setObject(nIndex + 1, objValue);
			}
			else {
				((PreparedStatement)m_stmt).setNull(nIndex + 1, value.getColumn().getType().getJdbcType());
			}
		}

		return (PreparedStatement)m_stmt;
	}

	/**
	 * This method returns a statement. If the statement has not been generated, it will be and be returned.
	 * 
	 * @throws SQLException
	 */
	protected Statement getStatement(final Connection conn) throws SQLException {
		if (m_stmt == null) {
			m_stmt = conn.createStatement();
		}

		return m_stmt;
	}
}

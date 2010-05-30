package com.tippingpoint.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.DataConversion;
import com.tippingpoint.database.DatabaseException;

/**
 * This class is used to execute a SQL statement.
 */
public class SqlExecution {
	private static final Log m_log = LogFactory.getLog(SqlExecution.class);

	/** This member holds the data conversion used to convert the parameterized values to database values. */
	protected DataConversion m_conversion;

	/** This member holds the statement. */
	protected Statement m_stmt;

	/** This member holds the text of the statement being executed. */
	protected String m_strStatement;

	/** This member holds the mapping of the columns to the index of the column in the SQL statement. */
	private final Map<Column, Integer> m_mapColumns = new HashMap<Column, Integer>();

	/**
	 * This method constructs an execution of a statement.
	 */
	public SqlExecution(final DataConversion conversion) {
		this(conversion, null);
	}

	/**
	 * This method constructs an execution of a statement.
	 */
	public SqlExecution(final DataConversion conversion, final String strStatement) {
		m_conversion = conversion;

		setStatement(strStatement);
	}

	/**
	 * This method adds a column to the column map. It is assumed that the SQL exists that corresponds to this mapping.
	 */
	public void addColumnMap(final Column column, final Integer intIndex) {
		m_mapColumns.put(column, intIndex);
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
	 * @throws SQLException
	 */
	public ResultSet executeQuery(final Connection conn) throws SQLException {
		final Statement stmt = getStatement(conn);

		stmt.execute(m_strStatement);

		return stmt.getResultSet();
	}

	/**
	 * This method executes the statement.
	 * 
	 * @throws SqlExecutionException
	 */
	public int executeUpdate(final Connection conn) throws SqlExecutionException {
		int nRowsUpdated = 0;

		try {
			final Statement stmt = getStatement(conn);

			nRowsUpdated = stmt.executeUpdate(m_strStatement);
		}
		catch (final SQLException e) {
			throw new SqlExecutionException(m_strStatement, e);
		}

		return nRowsUpdated;
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
	 * This method returns the conversion instance in use by this SQL execution.
	 */
	public DataConversion getConversion() {
		return m_conversion;
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
			objValue = m_conversion.getObject(column.getType(), rs, intIndex);
		}

		return objValue;
	}

	/**
	 * This method returns the statement being executed.
	 */
	public String getStatement() {
		return m_strStatement;
	}

	/**
	 * This method sets the statement for execution.
	 */
	public void setStatement(final String strStatement) {
		m_strStatement = strStatement;
	}

	/**
	 * This method dumps the SQL to the command being executed.
	 */
	@Override
	public String toString() {
		return m_strStatement;
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

package com.tippingpoint.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.DataConversion;

/**
 * This class is used to execute a statement with parameterized values.
 */
public final class SqlParameterizedExecution extends SqlExecution {
	/**
	 * This member holds the map of the columns and their values.
	 */
	private final List<ParameterizedValue> m_listParameters = new ArrayList<ParameterizedValue>();

	/**
	 * This method constructs an execution of a statement. It is intended that a conversion will most likely be used to
	 * convert data being placed in the query.
	 */
	public SqlParameterizedExecution(final DataConversion conversion) {
		super(conversion);
	}

	/**
	 * This method adds a parameterized value to the collection.
	 */
	public void add(final ParameterizedValue value) {
		m_listParameters.add(value);
	}

	/**
	 * This method executes the statement and returns the resulting result set.
	 * 
	 * @throws SQLException
	 */
	@Override
	public ResultSet executeQuery(final Connection conn) throws SQLException {
		final PreparedStatement pstmt = (PreparedStatement)getStatement(conn);

		return pstmt.executeQuery();
	}

	/**
	 * This method executes the statement.
	 * 
	 * @throws SqlExecutionException
	 */
	@Override
	public int executeUpdate(final Connection conn) throws SqlExecutionException {
		int nRowsUpdated = 0;

		try {
			final PreparedStatement pstmt = (PreparedStatement)getStatement(conn);
			nRowsUpdated = pstmt.executeUpdate();
		}
		catch (final SQLException e) {
			throw new SqlExecutionException(m_strStatement, e);
		}

		return nRowsUpdated;
	}

	/**
	 * This method returns an iterator over the values that are being set for the statement.
	 */
	public Iterator<ParameterizedValue> getValues() {
		return m_listParameters.iterator();
	}

	/**
	 * This method sets the value for the given column.
	 * 
	 * @param column Column for which to set the value.
	 * @param Object objValue new value to set for the column.
	 */
	public void setValue(final Column column, final Object objValue) {
		// find the value corresponding to the passed in column
		for (int nIndex = 0; nIndex < m_listParameters.size(); ++nIndex) {
			if (m_listParameters.get(nIndex).getColumn().equals(column)) {
				m_listParameters.get(nIndex).setValue(objValue);
				break;
			}
		}
	}

	/**
	 * This method returns a statement. If the statement has not been generated, it will be and be returned.
	 * 
	 * @throws SQLException
	 */
	@Override
	protected Statement getStatement(final Connection conn) throws SQLException {
		if (m_stmt == null) {
			m_stmt = conn.prepareStatement(getStatement());
		}

		// add all the parameters to the prepared statement
		for (int nIndex = 0; nIndex < m_listParameters.size(); nIndex++ ) {
			final ParameterizedValue value = m_listParameters.get(nIndex);
			final Object objValue = m_conversion.convertToSqlObject(value.getColumn().getType(), value.getValue());

			if (objValue != null) {
				((PreparedStatement)m_stmt).setObject(nIndex + 1, objValue);
			}
			else {
				((PreparedStatement)m_stmt).setNull(nIndex + 1, value.getColumn().getType().getJdbcType());
			}
		}

		return m_stmt;
	}
}

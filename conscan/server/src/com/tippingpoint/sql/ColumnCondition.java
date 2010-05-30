package com.tippingpoint.sql;

import com.tippingpoint.database.Column;

/**
 * This class represents a condition binding a column to a value.
 */
public final class ColumnCondition extends Condition {
	/** This member holds the column for which this condition exists. */
	private final Column m_column;

	/** This member holds the operation used for the condition. */
	private final Column m_column2;

	/** This member holds the operation used for the condition. */
	private final Operation m_operation;

	/**
	 * This method constructs a new condition for 2 named columns.
	 */
	public ColumnCondition(final Column column, final Operation operation, final Column column2) {
		m_column = column;
		m_column2 = column2;
		m_operation = operation;
	}

	/**
	 * This method returns the value associated with the condition.
	 */
	@Override
	public ParameterizedValue getParameterValue() {
		return new ParameterizedValue(m_column, m_column2);
	}

	/**
	 * This method returns if the condition includes a parameterized value.
	 */
	@Override
	public boolean hasParameter() {
		return false;
	}

	/**
	 * This method creates the statement portion used to add to the SQL.
	 */
	@Override
	protected String createStatement() {
		return m_operation.toString(m_column, m_column2);
	}
}

package com.tippingpoint.sql;

import com.tippingpoint.database.Column;

/**
 * This class represents a condition binding a column to a value.
 */
public final class ValueCondition extends Condition {
	/** This member holds the column for which this condition exists. */
	private final Column m_column;

	/** This member holds the value for which this column is bound. */
	private final Object m_objValue;

	/** This member holds the operation used for the condition. */
	private final Operation m_operation;

	/**
	 * This method constructs a new condition for the named column and value.
	 */
	public ValueCondition(final Column column, final Operation operation, final Object objValue) {
		m_column = column;
		m_operation = operation;
		m_objValue = objValue;
	}

	/**
	 * This returns the value associated with the condition.
	 */
	@Override
	public ParameterizedValue getParameterValue() {
		return new ParameterizedValue(m_column, m_objValue);
	}

	/**
	 * This method returns if the condition includes a parameterized value.
	 */
	@Override
	public boolean hasParameter() {
		return true;
	}

	/**
	 * This method creates the statement portion used to add to the SQL.
	 */
	@Override
	protected String createStatement() {
		return m_operation.toString(m_column, "?");
	}
}

package com.tippingpoint.sql;

import com.tippingpoint.database.Column;

/**
 * This class is used to hold the column mapping of a parameterized value.
 */
public final class ParameterizedValue {
	/** This member holds the column for which the value is being set. */
	private final Column m_column;

	/** This member holds the value. */
	private Object m_objValue;

	/**
	 * This method constructs a new value.
	 */
	public ParameterizedValue(final Column column, final Object objValue) {
		m_column = column;
		m_objValue = objValue;
	}

	/**
	 * This method is used to return the column associated with the parameterized value.
	 */
	public Column getColumn() {
		return m_column;
	}

	/**
	 * This method is used to return the raw parameterized value.
	 */
	public Object getValue() {
		return m_objValue;
	}

	/**
	 * This method sets the parameterized value.
	 * 
	 * @param value Object containing the new raw value.
	 */
	public void setValue(final Object objValue) {
		m_objValue = objValue;
	}
}

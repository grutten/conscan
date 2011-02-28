package com.tippingpoint.sql;

import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnTypeId;

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
	public ParameterizedValue(final Column column, Object objValue) {
		m_column = column;

		if (column.getType() instanceof ColumnTypeId && objValue == null && !column.getType().idDerived()) {
			objValue = ConnectionManagerFactory.getFactory().getDefaultManager().getIdFactory().getNewValue();
		}

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

	/**
	 * This method dumps the parameterized value in a readable fashion.
	 */
	@Override
	public String toString() {
		return m_column.toString() + "=" + (m_objValue != null ? m_objValue.toString() : "null");
	}
}

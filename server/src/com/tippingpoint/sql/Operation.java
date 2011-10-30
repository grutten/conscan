package com.tippingpoint.sql;

import com.tippingpoint.database.Column;

/**
 * This class represents an operation performed in a SQL statement.
 */
public abstract class Operation {
	public static final Operation EQUALS = new BinaryOperation("=");

	/** This member holds the string representing the operation. */
	private final String m_strOperation;

	/**
	 * This method constructs a new operation. The string is the database string used in the SQL.
	 */
	protected Operation(final String strOperation) {
		m_strOperation = strOperation;
	}

	/**
	 * This method returns the operation.
	 */
	@Override
	public String toString() {
		return m_strOperation;
	}

	/**
	 * This method is used to construct a phrase using the operation for the passed in operands.
	 */
	public String toString(final Object objLhs, final Object objRhs) {
		final StringBuilder strBuffer = new StringBuilder();

		add(strBuffer, objLhs);
		strBuffer.append(' ').append(this).append(' ');
		add(strBuffer, objRhs);

		return strBuffer.toString();
	}

	/**
	 * This method adds the object to the string; if the object is a column the fully qualified name is used.
	 */
	private void add(final StringBuilder strBuffer, final Object objValue) {
		if (objValue instanceof Column) {
			strBuffer.append(((Column)objValue).getFQName());
		}
		else if (objValue != null) {
			strBuffer.append(objValue);
		}
	}
}

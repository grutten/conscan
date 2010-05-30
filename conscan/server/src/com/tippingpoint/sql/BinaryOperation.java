package com.tippingpoint.sql;

/**
 * This class represents an operation binary performed in a SQL statement.
 */
public final class BinaryOperation extends Operation {
	/**
	 * This method constructs a new operation. The string is the database string used in the SQL.
	 */
	protected BinaryOperation(final String strOperation) {
		super(strOperation);
	}

	/**
	 * This method is used to construct a phrase using the operation for the passed in operands.
	 */
	@Override
	public String toString(final Object objLhs, final Object objRhs) {
		if (objLhs == null) {
			throw new IllegalArgumentException("SQL operations must have at least one operand.");
		}
		if (objRhs == null) {
			throw new IllegalArgumentException("A right hand operand is necessary for binary SQL operations.");
		}

		return super.toString(objLhs, objRhs);
	}
}

package com.tippingpoint.sql;

/**
 * This class represents a conditional added to a SQL statement.
 */
public abstract class Condition {
	/**
	 * This method returns the value associated with the condition.
	 */
	public ParameterizedValue getParameterValue() {
		// default action is to return nothing
		return null;
	}

	/**
	 * This method returns if the condition includes a parameterized value.
	 */
	public abstract boolean hasParameter();

	/**
	 * This method dumps the condition as a string.
	 */
	@Override
	public String toString() {
		return createStatement();
	}

	/**
	 * This method creates the statement used to generate the table.
	 * 
	 * @throws SqlBuilderException
	 */
	protected abstract String createStatement();
}

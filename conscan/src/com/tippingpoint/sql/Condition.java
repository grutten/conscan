package com.tippingpoint.sql;

/**
 * This class represents a conditional added to a SQL statement.
 */
public abstract class Condition {
	/**
	 * This method adds the column to the execution instance.
	 */
	public abstract void getExecution(SqlParameterizedExecution sql);

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

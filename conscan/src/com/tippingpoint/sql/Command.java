package com.tippingpoint.sql;

/**
 * This class is a base class of all SQL command classes.
 */
public abstract class Command {
	public static final String SQL_NULL = "NULL";

	/** This member holds the string version of the statement. */
	protected SqlExecution m_sql;

	/**
	 * This method returns a class used to execute the statement.
	 * 
	 * @throws SqlBuilderException
	 */
	public SqlExecution getExecution() throws SqlBuilderException {
		if (m_sql == null) {
			m_sql = createExecution();
		}

		return m_sql;
	}

	/**
	 * This method dumps the sql statement.
	 */
	@Override
	public String toString() {
		String strValue = null;

		try {
			final SqlExecution sql = getExecution();
			if (sql != null) {
				strValue = sql.toString();
			}
		}
		catch (final SqlBuilderException e) {
			strValue = new StringBuilder().append("Unable to generate a SQL statement. ").append(e).toString();
		}

		return strValue;
	}

	/**
	 * This method creates the statement used to execution the SQL command.
	 * 
	 * @throws SqlBuilderException
	 */
	protected abstract SqlExecution createExecution() throws SqlBuilderException;

	/**
	 * This method resets the command so that it will get regenerated.
	 */
	protected void reset() {
		m_sql = null;
	}
}

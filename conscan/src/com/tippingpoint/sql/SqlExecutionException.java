package com.tippingpoint.sql;

/**
 * SqlExecutionException This class is used to flag SQL related exceptions when executing SQL.
 */
public class SqlExecutionException extends SqlBaseException {
	private static final long serialVersionUID = 2563138703712325326L;

	/** This member holds the SQL that was executing when the exception occurred. */
	private final String m_strSql;

	/**
	 * This method constructs a new method with the passed in message for the given SQL.
	 */
	public SqlExecutionException(final String strSql, final Throwable t) {
		super(t);

		m_strSql = strSql;
	}

	/**
	 * This method returns the detail message string of this exception.
	 */
	@Override
	public String getMessage() {
		return new StringBuilder().append("Error executing the SQL: ").append(m_strSql).toString();
	}
}

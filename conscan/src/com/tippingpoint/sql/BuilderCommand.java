package com.tippingpoint.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * BuilderCommand
 */
public abstract class BuilderCommand extends Command {
	/** This member holds the list of conditions for the query. */
	protected List<Condition> m_listWheres = new ArrayList<Condition>();

	/**
	 * This method constructs a new command for the given builder.
	 */
	protected BuilderCommand() {
	}

	/**
	 * This method adds a condition (i.e. a where clause) to the list of conditions for the select.
	 */
	public void add(Condition condition) {
		m_listWheres.add(condition);
		reset();
	}

	/**
	 * This method is used to add the where clauses to the SQL statement.
	 */
	protected void addWheres(StringBuilder strSql, SqlExecution sql) {
		if (m_listWheres != null && !m_listWheres.isEmpty()) {
			strSql.append(" WHERE ");

			Iterator<Condition> iterWheres = m_listWheres.iterator();
			while (iterWheres.hasNext()) {
				Condition condition = iterWheres.next();

				strSql.append(condition);

				if (condition.hasParameter()) {
					condition.getExecution((SqlParameterizedExecution)sql);
				}

				if (iterWheres.hasNext()) {
					strSql.append(" AND ");
				}
			}
		}
	}

	/**
	 * This method determines if the SQL has parameterized values.
	 */
	protected boolean isParameterized() {
		boolean bParameterized = false;

		if (m_listWheres != null && !m_listWheres.isEmpty()) {
			Iterator<Condition> iterWheres = m_listWheres.iterator();
			while (iterWheres.hasNext() && !bParameterized) {
				bParameterized = iterWheres.next().hasParameter();
			}
		}

		return bParameterized;
	}
}

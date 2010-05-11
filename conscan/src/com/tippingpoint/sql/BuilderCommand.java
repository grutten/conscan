package com.tippingpoint.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * BuilderCommand
 */
public abstract class BuilderCommand extends Command {
	/** This member holds the list of conditions for the query. */
	private final List<Condition> m_listWheres = new ArrayList<Condition>();

	/**
	 * This method constructs a new command for the given builder.
	 */
	protected BuilderCommand() {
	}

	/**
	 * This method adds a condition (i.e. a where clause) to the list of conditions for the select.
	 */
	public void add(final Condition condition) {
		m_listWheres.add(condition);
	}

	/**
	 * This method returns the list of where conditions.
	 */
	public List<Condition> getWheres() {
		return m_listWheres;
	}

	/**
	 * This method determines if the SQL has parameterized values.
	 */
	protected boolean isParameterized() {
		boolean bParameterized = false;

		if (m_listWheres != null && !m_listWheres.isEmpty()) {
			final Iterator<Condition> iterWheres = m_listWheres.iterator();
			while (iterWheres.hasNext() && !bParameterized) {
				bParameterized = iterWheres.next().hasParameter();
			}
		}

		return bParameterized;
	}
}

package com.tippingpoint.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.Table;

/**
 * This class defines the methods needed to create a SQL query statement.
 */
public class SqlQuery extends BuilderCommand {
	/** This member holds a flag indicating if associative joins should be pursued. */
	private boolean m_bAssociativeJoins;

	/** This member holds the list of columns that are ordering the query. */
	private final List<Column> m_listOrderByColumns = new ArrayList<Column>();

	/** This member holds the list of columns that are being selected. */
	private final List<Column> m_listQueryColumns = new ArrayList<Column>();

	/** This member holds the list of tables that are being queried. */
	private final Set<Table> m_setTables = new LinkedHashSet<Table>();

	/**
	 * This method constructs a new query instance.
	 */
	public SqlQuery() {
	}

	/**
	 * This method adds a column to the list of columns for the select.
	 */
	public final void add(final Column column) {
		if (column == null) {
			throw new IllegalArgumentException("Column must be specified.");
		}

		m_listQueryColumns.add(column);
	}

	/**
	 * This method adds a table to the list of tables for the select.
	 */
	public final void add(final Table table) {
		add(table, false);
	}

	/**
	 * This method adds a table to the list of tables for the select. It also optionally adds all the columns of the
	 * given table to the list of columns for the select.
	 */
	public final void add(final Table table, final boolean bAddColumns) {
		m_setTables.add(table);

		if (bAddColumns) {
			final Iterator<Column> iterColumns = table.getColumns();
			if (iterColumns != null) {
				while (iterColumns.hasNext()) {
					add(iterColumns.next());
				}
			}
		}
	}

	public List<Column> getOrderByColumns() {
		return m_listOrderByColumns;
	}

	public List<Column> getQueryColumns() {
		return m_listQueryColumns;
	}

	public Set<Table> getTables() {
		return m_setTables;
	}

	/**
	 * This method adds an order by column entry to the query.
	 * 
	 * @param column Column to be added to the order by
	 */
	public void orderBy(final Column column) {
		if (column == null) {
			throw new IllegalArgumentException("Column must be specified.");
		}

		m_listOrderByColumns.add(column);
	}

	/**
	 * This method sets the flag indicating if associative joins should be pursued.
	 */
	public void setAssociativeJoins(final boolean bAssociativeJoins) {
		m_bAssociativeJoins = bAssociativeJoins;
	}

	public boolean useAssociativeJoins() {
		return m_bAssociativeJoins;
	}

	/**
	 * This method returns the string representation of the SQL command.
	 */
	public String toString() {
		StringBuilder strBuffer = new StringBuilder();
		
		strBuffer.append("SELECT");
		
		for (Column column : m_listQueryColumns) {
			strBuffer.append(" ").append(column);
		}

		if (!m_setTables.isEmpty()) {
			strBuffer.append(" FROM");
			for (Table table : m_setTables) {
				strBuffer.append(" ").append(table);
			}
		}

		if (!m_listWheres.isEmpty()) {
			strBuffer.append(" WHERE");
			for (Condition condition : m_listWheres) {
				strBuffer.append(" ").append(condition);
			}
		}

		if (!m_listOrderByColumns.isEmpty()) {
			strBuffer.append(" ORDER BY");
			for (Column column : m_listOrderByColumns) {
				strBuffer.append(" ").append(column);
			}
		}

		return strBuffer.toString();
	}
}

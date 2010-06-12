package com.tippingpoint.sql.base;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ForeignKey;
import com.tippingpoint.database.ForeignKeyConstraint;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.ColumnCondition;
import com.tippingpoint.sql.Operation;
import com.tippingpoint.sql.SqlBuilderException;
import com.tippingpoint.sql.SqlQuery;

public class SqlQueryExecution extends SqlExecution {
	/** This member holds the source of the command. */
	private final SqlQuery m_sqlQuery;

	/**
	 * This method constructs a new execution for the given manager.
	 */
	public SqlQueryExecution(final SqlManager sqlManager, final SqlQuery sqlQuery) {
		super(sqlManager);

		m_sqlQuery = sqlQuery;
	}

	/**
	 * This method is used to generated the SQL statement.
	 * 
	 * @throws SqlBuilderException
	 */
	@Override
	public String getSql() throws SqlBuilderException {
		final StringBuilder strSql = new StringBuilder();

		strSql.append("SELECT ");

		final List<Column> listQueryColumns = m_sqlQuery.getQueryColumns();
		if (!listQueryColumns.isEmpty()) {
			final Iterator<Column> iterColumns = listQueryColumns.iterator();
			while (iterColumns.hasNext()) {
				final Column column = iterColumns.next();

				strSql.append(column);
				addColumnMap(column);

				if (iterColumns.hasNext()) {
					strSql.append(", ");
				}
			}
		}

		final Set<Table> tables = m_sqlQuery.getTables();
		if (!tables.isEmpty()) {
			strSql.append(" FROM ");

			if (m_sqlQuery.useAssociativeJoins()) {
				addAssociativeJoins(tables);
			}

			final Iterator<Table> iterTables = tables.iterator();
			while (iterTables.hasNext()) {
				final Table table = iterTables.next();

				strSql.append(table);

				if (iterTables.hasNext()) {
					strSql.append(", ");
				}

				addJoins(tables, table);
			}
		}

		addWheres(m_sqlQuery.getWheres(), strSql);
		addOrderBys(m_sqlQuery.getOrderByColumns(), strSql);

		return strSql.toString();
	}

	/**
	 * This method adds tables that appear to associative tables. This is determined by adding tables that are
	 * referenced by foreign keys in more than 1 existing table.
	 * 
	 * @param tables
	 */
	private void addAssociativeJoins(final Set<Table> tables) {
		final Map<Table, Integer> mapReferencedTables = new HashMap<Table, Integer>();

		Iterator<Table> iterTables = tables.iterator();
		while (iterTables.hasNext()) {
			final Table table = iterTables.next();

			final List<ForeignKeyConstraint> listReferences = table.getReferences();
			if (listReferences != null && !listReferences.isEmpty()) {
				for (int nIndex = 0; nIndex < listReferences.size(); ++nIndex) {
					final ForeignKeyConstraint constraint = listReferences.get(nIndex);
					final Table tableReferenced = constraint.getTable();

					if (!tables.contains(tableReferenced)) {
						Integer intCount = mapReferencedTables.get(tableReferenced);

						if (intCount != null) {
							intCount = new Integer(intCount.intValue() + 1);
						}
						else {
							intCount = new Integer(1);
						}

						mapReferencedTables.put(tableReferenced, intCount);
					}
				}
			}
		}

		if (!mapReferencedTables.isEmpty()) {
			iterTables = mapReferencedTables.keySet().iterator();
			while (iterTables.hasNext()) {
				final Table tableReferenced = iterTables.next();

				if (mapReferencedTables.get(tableReferenced).intValue() > 1) {
					tables.add(tableReferenced);
				}
			}
		}
	}

	/**
	 * This method adds the condition statements used to join tables.
	 * 
	 * @param tables Set containing all the tables in the query
	 * @param table Table that is currently in the query.
	 */
	private void addJoins(final Set<Table> tables, final Table table) {
		if (tables.size() > 1) {
			final List<ForeignKeyConstraint> listReferences = table.getReferences();
			if (listReferences != null && !listReferences.isEmpty()) {
				for (int nIndex = 0; nIndex < listReferences.size(); ++nIndex) {
					final ForeignKeyConstraint constraint = listReferences.get(nIndex);

					if (tables.contains(constraint.getTable())) {
						final Iterator<Column> iterColumns = constraint.getColumns();
						if (iterColumns != null && iterColumns.hasNext()) {
							while (iterColumns.hasNext()) {
								final ForeignKey key = (ForeignKey)iterColumns.next();

								m_sqlQuery.add(new ColumnCondition(key.getParentColumn(), Operation.EQUALS, key
										.getChildColumn()));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * This method adds the order by clauses to the statement.
	 * 
	 * @param listOrderByColumns List containing the columns to add.
	 * @param strSql StringBuilder containing the current SQL statement.
	 */
	private void addOrderBys(final List<Column> listOrderByColumns, final StringBuilder strSql) {
		if (listOrderByColumns != null && !listOrderByColumns.isEmpty()) {
			strSql.append(" ORDER BY ");

			final Iterator<Column> iterColumns = listOrderByColumns.iterator();
			while (iterColumns.hasNext()) {
				final Column column = iterColumns.next();

				strSql.append(column);

				if (iterColumns.hasNext()) {
					strSql.append(", ");
				}
			}
		}
	}
}

package com.tippingpoint.database;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents the collection of tables for a given schema.
 */
public final class Schema extends Element {
	private static Log m_log = LogFactory.getLog(Schema.class);

	/** This member holds all the tables in the schema. */
	private final Map<String, Table> m_tables = new LinkedHashMap<String, Table>();

	/**
	 * This method creates a new schema.
	 */
	public Schema() {
	}

	/**
	 * This method creates a new schema with the given name.
	 */
	public Schema(final String strName) {
		super(strName);
	}

	/**
	 * This method adds a table to the schema.
	 */
	public void addTable(final Table table) {
		m_tables.put(table.getName(), table);
	}

	/**
	 * This method returns a named table in the schema.
	 */
	public Table getTable(final String strName) {
		final Table table = m_tables.get(strName);

		if (table == null) {
			m_log.error("Error retreiving table '" + strName + "'");
		}

		return table;
	}

	/**
	 * This method returns the number of tables in the schema.
	 * 
	 * @return Count of tables defined in the schema.
	 */
	public int getTableCount() {
		return m_tables.size();
	}

	/**
	 * @return Returns the tables.
	 */
	public Iterator<Table> getTables() {
		return m_tables.values().iterator();
	}
}

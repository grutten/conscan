package com.tippingpoint.conscan.objects;

import com.tippingpoint.database.Table;

/**
 * This class returns business object associated with database table.
 */
public class TableBusinessObjectBuilder implements BusinessObjectBuilder {
	/** This member holds the persistence mechanism used in the business object. */
	private final TablePersistence m_tablePersistence;

	/**
	 * This method constructs a new a builder for the object associated with the passed in table.
	 * 
	 * @param table Table for which the object should be created.
	 */
	public TableBusinessObjectBuilder(final Table table) {
		m_tablePersistence = new TablePersistence(table);
	}

	/**
	 * This method returns the name of the object being created. It must be unique in the system.
	 */
	@Override
	public BusinessObject get() {
		return new BusinessObjectImpl(m_tablePersistence);
	}

	/**
	 * This method returns a business object instance.
	 */
	@Override
	public String getName() {
		return m_tablePersistence.getTable().getName();
	}

}

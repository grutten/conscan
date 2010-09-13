package com.tippingpoint.conscan.objects;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TableBusinessObject extends BusinessObjectImpl {
	/** This member holds the type of the object. */
	private final String m_strType;

	/**
	 * This method constructs a new business object with the given persistence.
	 * 
	 * @param persistence Persistence instance to use to write/read the object.
	 */
	public TableBusinessObject(final TablePersistence persistence) {
		this(persistence, new LinkedHashMap<String, FieldValue>());
	}

	/**
	 * This method constructs a new business object with the given persistence and initial values
	 * 
	 * @param persistence Persistence instance to use to write/read the object.
	 * @param mapValues Map<String, FieldValue> containing the initial values of the business object.
	 */
	public TableBusinessObject(final TablePersistence persistence, final Map<String, FieldValue> mapValues) {
		super(persistence, mapValues);

		m_strType = persistence.getType();
	}

	/**
	 * This method returns the type of business object.
	 */
	@Override
	public String getType() {
		return m_strType;
	}
}

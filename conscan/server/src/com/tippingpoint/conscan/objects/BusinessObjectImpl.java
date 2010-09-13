package com.tippingpoint.conscan.objects;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import com.tippingpoint.sql.SqlBaseException;

/**
 * This class is an implementation of a business object.
 */
public abstract class BusinessObjectImpl extends BusinessObject {
	/** This member holds the field values of the object. */
	private final Map<String, FieldValue> m_mapValues;

	/** This member holds the persistence mechanism used for this business object. */
	private final Persistence m_persistence;

	/**
	 * This method constructs a new business object with the given persistence.
	 * 
	 * @param persistence Persistence instance to use to write/read the object.
	 */
	public BusinessObjectImpl(final Persistence persistence) {
		this(persistence, new LinkedHashMap<String, FieldValue>());
	}

	/**
	 * This method constructs a new business object with the given persistence and initial values
	 * 
	 * @param persistence Persistence instance to use to write/read the object.
	 * @param mapValues Map<String, FieldValue> containing the initial values of the business object.
	 */
	public BusinessObjectImpl(final Persistence persistence, final Map<String, FieldValue> mapValues) {
		m_persistence = persistence;
		m_mapValues = mapValues;
	}

	/**
	 * This method returns a list of all the named elements of the business object.
	 */
	@Override
	public Iterator<String> getFields() {
		return m_persistence.getFields();
	}

	/**
	 * This method returns the identifier field value for this business object. If the object does not have an
	 * identifier, this may return null.
	 */
	@Override
	public FieldValue getIdentifierField() {
		FieldValue fieldValue = null;

		final String strIdentifierName = m_persistence.getIdentifierName();
		if (strIdentifierName != null) {
			fieldValue = getValue(strIdentifierName);
		}

		return fieldValue;
	}

	/**
	 * This method returns the value for the named field.
	 * 
	 * @param strName String containing the name.
	 */
	@Override
	public FieldValue getValue(final String strName) {
		return m_mapValues.get(strName);
	}

	/**
	 * This method returns an iterator over the value for the fields.
	 */
	@Override
	public Iterator<FieldValue> getValues() {
		return m_mapValues.values().iterator();
	}

	/**
	 * This method saves the object, if necessary.
	 * 
	 * @throws SqlBaseException
	 */
	@Override
	public final void save() throws SqlBaseException {
		if (isDirty()) {
			m_persistence.save(m_mapValues);
		}
	}

	/**
	 * This method sets the named field value.
	 * 
	 * @param strName String containing the name of the field.
	 * @param objValue Object containing the value.
	 */
	@Override
	public Object setValue(final String strName, final Object objValue) {
		FieldValue value = m_mapValues.get(strName);
		if (value == null) {
			value = new FieldValue(strName);
			m_mapValues.put(strName, value);
		}

		return value.setValue(objValue);
	}

	/**
	 * This method returns if any of the fields have changed.
	 */
	private boolean isDirty() {
		boolean bDirty = false;

		final Iterator<FieldValue> iterValues = m_mapValues.values().iterator();
		if (iterValues != null && iterValues.hasNext()) {
			while (iterValues.hasNext() && !bDirty) {
				bDirty = iterValues.next().isDirty();
			}
		}

		return bDirty;
	}
}

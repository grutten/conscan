package com.tippingpoint.conscan.objects;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.tippingpoint.sql.SqlBaseException;

/**
 * This class is an implementation of a business object.
 */
public class BusinessObjectImpl extends BusinessObject {
	/** This member holds the field values of the object. */
	private final Map<String, FieldValue> m_mapValues = new HashMap<String, FieldValue>();

	/** This member holds the persistence mechanism used for this business object. */
	private final Persistence m_persistence;

	/**
	 * This method constructs a new business object with the given persistence.
	 * 
	 * @param persistence
	 */
	public BusinessObjectImpl(final Persistence persistence) {
		m_persistence = persistence;
	}

	/**
	 * This method returns a list of all the named elements of the business object.
	 */
	@Override
	public Iterator<String> getFields() {
		return m_persistence.getFields();
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
			value = new FieldValue();
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

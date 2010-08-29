package com.tippingpoint.conscan.objects;

import org.apache.commons.lang.ObjectUtils;

/**
 * This class is used to have a value for a field.
 */
public class FieldValue {
	/** This member holds if this value has changed. */
	private boolean m_bDirty;

	/** This member holds the actual value. */
	private Object m_objValue;

	/**
	 * This method constructs a new field value with a null value.
	 */
	public FieldValue() {
		this(null);
	}

	/**
	 * This method constructs a new field value for the given value. If this constructor is used, the field is assumed
	 * to be clean.
	 * 
	 * @param objValue Object containing the initial value of the field.
	 */
	public FieldValue(final Object objValue) {
		m_objValue = objValue;
		m_bDirty = false;
	}

	/**
	 * This method returns the value.
	 */
	public Object getValue() {
		return m_objValue;
	}

	/**
	 * This method returns if this value is dirty.
	 */
	public boolean isDirty() {
		return m_bDirty;
	}

	/**
	 * This method sets the value.
	 */
	public Object setValue(final Object objValue) {
		final Object objReturn = m_objValue;

		if (!ObjectUtils.equals(m_objValue, objValue)) {
			m_objValue = objValue;
			m_bDirty = true;
		}

		return objReturn;
	}

	/**
	 * This method returns a string representation of the value.
	 */
	@Override
	public String toString() {
		return (m_objValue != null ? m_objValue.toString() : "<null>") + (m_bDirty ? "(changed)" : "");
	}
}

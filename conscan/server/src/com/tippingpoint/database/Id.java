package com.tippingpoint.database;

import org.apache.commons.lang.ObjectUtils;

/**
 * This class is used to hold the identify of a database object.
 */
public class Id {
	public static final Id UNKNOWN_ID = new Id(-1);

	/** This member holds the identifier. */
	private Integer m_intValue;
	
	/** This member holds the string version of the identifier. */
	private String m_strValue;

	/**
	 * This method constructs a new ID object.
	 */
	public Id(final Integer intValue) {
		m_intValue = intValue;
		m_strValue = intValue.toString();
	}

	/**
	 * This method constructs a new ID object.
	 */
	public Id(final String strValue) {
		try {
			m_intValue = new Integer(strValue);
			m_strValue = m_intValue.toString();
		}
		catch (NumberFormatException e) {
			// ignore number exception and just store the string
			m_strValue = strValue;
		}
	}

	/**
	 * This method returns if the two values are identical.
	 */
	public boolean equals(final Id idRhs) {
		return ObjectUtils.equals(m_intValue, idRhs.m_intValue);
	}

	/**
	 * This method returns if the two values are identical.
	 */
	@Override
	public boolean equals(final Object objRhs) {
		return objRhs instanceof Id && equals((Id)objRhs);
	}

	/**
	 * This method returns the value of the id.
	 */
	public Object getValue() {
		return m_intValue != null ? m_intValue : m_strValue;
	}

	/**
	 * This method returns this object as a string.
	 */
	@Override
	public String toString() {
		return m_strValue != null ? m_strValue : "<unknown>";
	}
}
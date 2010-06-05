package com.tippingpoint.utilities;

import org.apache.commons.lang.StringUtils;

/**
 * This class holds a name/value pair.
 */
public class NameValuePair {
	/** This member holds the name of the pair. */
	private String m_strName = null;

	/** This member holds the value of the pair. */
	private String m_strValue = null;

	/**
	 * This method constructs an empty name/value pair.
	 */
	public NameValuePair() {
		this(null, null);
	}

	/**
	 * This method constructs a name/value pair with the given values.
	 * 
	 * @param strName String containing the name of the pair.
	 * @param strValue String containing the value of the pair.
	 */
	public NameValuePair(final String strName, final String strValue) {
		setName(strName);
		setValue(strValue);
	}

	/**
	 * This method returns if this instance is the same as the passed in instance.
	 * 
	 * @param objValue Object containing the target elements.
	 */
	public boolean equals(final NameValuePair pair) {
		boolean bEquals = this == pair;

		if (!bEquals) {
			bEquals = StringUtils.equals(m_strName, pair.m_strName) && StringUtils.equals(m_strValue, pair.m_strValue);
		}

		return bEquals;
	}

	/**
	 * This method returns if this instance is the same as the passed in instance.
	 * 
	 * @param objValue Object containing the target elements.
	 */
	@Override
	public boolean equals(final Object objValue) {
		return objValue instanceof NameValuePair && equals((NameValuePair)objValue);
	}

	/**
	 * This method returns the name of the pair.
	 */
	public String getName() {
		return m_strName;
	}

	/**
	 * This method returns the value of the pair.
	 */
	public String getValue() {
		return m_strValue;
	}

	/**
	 * This method returns a hash code for this pair.
	 */
	@Override
	public int hashCode() {
		return this.getClass().hashCode() ^ (null == m_strName ? 0 : m_strName.hashCode()) ^
				(null == m_strValue ? 0 : m_strValue.hashCode());
	}

	/**
	 * This method sets the name of the pair.
	 * 
	 * @param strName String containing the name of the pair.
	 */
	public void setName(final String strName) {
		m_strName = strName;
	}

	/**
	 * This method sets the value of the pair.
	 * 
	 * @param strValue String containing the value of the pair.
	 */
	public void setValue(final String strValue) {
		m_strValue = strValue;
	}

	/**
	 * This method returns the string representation of this pair.
	 */
	@Override
	public String toString() {
		return "name=" + m_strName + ", " + "value=" + m_strValue;
	}
}

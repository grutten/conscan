package com.tippingpoint.database;

/**
 * This class is used as a base class for other database elements.
 */
public class Element {
	public static final String ATTRIBUTE_NAME = "name";

	/** This member holds the description of the element. */
	private String m_strDescription;

	/** This member holds the name of the element. */
	private String m_strName;

	/**
	 * This method constructs a new element. The name should be set before use.
	 */
	public Element() {
	}

	/**
	 * This method constructs a new element with the given name.
	 */
	public Element(final String strName) {
		setName(strName);
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return m_strDescription;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return m_strName;
	}

	/**
	 * @param strDescription The description to set.
	 */
	public void setDescription(final String strDescription) {
		m_strDescription = strDescription;
	}

	/**
	 * @param strName The name to set.
	 */
	public void setName(final String strName) {
		m_strName = strName;
	}

	/**
	 * This method returns the full name of the table.
	 */
	@Override
	public String toString() {
		return getName();
	}
}

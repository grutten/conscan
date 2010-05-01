package com.tippingpoint.database;

import org.apache.commons.lang.ClassUtils;

/**
 * This class is used to indicate an exception with the construction or state of a database element.
 */
public class DatabaseElementException extends DatabaseException {
	private static final long serialVersionUID = 8091322897134994625L;

	/**
	 * This method is used to construct a database element exception where the exception is defined in terms of
	 * attributes of the exception. This is typically used when an exception occurs while defining an element.
	 * 
	 * @param strName String containing the name of the attribute in error.
	 * @param clsElement Class defining what class is being constructed.
	 * @param strMessage Optional String containing a definition of the error.
	 */
	public DatabaseElementException(final String strName, final Class<?> clsElement, final String strMessage) {
		super("Type: " + ClassUtils.getShortClassName(clsElement) + " Element: " + strName +
				(strMessage != null ? " " + strMessage : ""));
	}
}

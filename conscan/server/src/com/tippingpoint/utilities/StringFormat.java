/*
 * StringFormat.java
 */

package com.tippingpoint.utilities;

/**
 * StringFormat This class is a utility class used to perform various actions on Strings.
 */
public class StringFormat {
	private StringFormat() {
		// utility class so nothing to do
	}

	/**
	 * This method checks the existence of a string.
	 * 
	 * @param strValue String containing the value to be checked.
	 * @return <code>true</code> if the string has any length.
	 */
	public static boolean isSpecified(final String strValue) {
		return strValue != null && strValue.length() > 0;
	}
}
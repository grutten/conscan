/*
 * StringFormat.java
 */

package com.tippingpoint.util.string;

import java.io.PrintWriter;
import java.util.regex.Pattern;

/**
 * StringFormat
 *
 * This class is a utility class used to perform various actions on Strings.
 */
public class StringFormat {
	/** This member contains the string used for a line separator. */
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static final char CHAR_COPY = 169;
	private static final char CHAR_REG = 174;

	private static final String EXCEPTION_CHARS = " '/-(#[{.\"";

	private static final char WILDCARD_DATABASE = '%';
	private static final char WILDCARD_LOCAL = '*';

	/**
	 * Regular expression used while escaping script tags.
	 */
	private static final Pattern REGEX_SCRIPT = Pattern.compile("(?i:<(script|iframe|frame))");
	
	/**
	 * Replacement string used with REGEX_SCRIPT
	 */
	private static final String REPLACE_STRING = "&lt;$1";
	
	private StringFormat() {
		// utility class so nothing to do
	}

	/**
	 * This method is used to add a formatted string to a buffer. This is
	 * helpful when trying to concatenate strings and needed to add
	 * separators if you have already adding something to the string.
	 *
	 * @param strBuffer StringBuffer containing the string to be added to.
	 * @param strSeparator String to use as a separator if there is something
	 * currently in the buffer.
	 * @param strValue String value to add. If this is null or 0 length, nothing
	 * is added to the buffer at all.
	 */
	public static void appendString(StringBuffer strBuffer, String strSeparator, String strValue) {
		appendString(strBuffer, strSeparator, null, strValue);
	}

	/**
	 * This method is used to add a formatted string to a buffer. This is
	 * helpful when trying to concatenate strings and needed to add
	 * separators if you have already adding something to the string. This
	 * version supports a prefix added to the beginning of the value.
	 *
	 * @param strBuffer StringBuffer containing the string to be added to.
	 * @param strSeparator String to use as a separator if there is something
	 * currently in the buffer.
	 * @param strPrefix String that is added immediately before the value.
	 * @param strValue String value to add. If this is null or 0 length, nothing
	 * is added to the buffer at all.
	 */
	public static void appendString(StringBuffer strBuffer, String strSeparator, String strPrefix, String strValue) {
		// only change the buffer if a value has been specified
		if (strValue != null && strValue.length() > 0) {
			// append the separator if anything is currently in the buffer
			if (strBuffer.length() > 0 && strSeparator != null)
				strBuffer.append(strSeparator);

			// add the prefix if specified
			if (strPrefix != null && strPrefix.length() > 0)
				strBuffer.append(strPrefix);

			// finally, add the new string
			strBuffer.append(strValue);
		}
	}

	/**
	 * Returns if the string contains only numerics.
	 */
	public static boolean containsOnlyDigits(String str) {
		boolean bOnlyDigits = false;
		str = str.trim();

		if (str.length() > 0) {
			bOnlyDigits = true;
			for (int i = 0; i < str.length() && bOnlyDigits; i++) {
				final char c = str.charAt(i);

				if (c < '0' || c > '9')
					bOnlyDigits = false;
			}
		}

		return bOnlyDigits;
	}

	/**
	 * This method returns the ellipsified string value for an object.
	 * @param obj Object value which to ellipsify
	 * @param nMaxLength Maximum length of object value before it's ellipsified
	 */
	public static String ellipsify(Object obj, int nMaxLength) {
		String strValue = obj != null ? obj.toString() : null;
		if (strValue != null) {
			if (nMaxLength > 0 && strValue.length() > nMaxLength) {
				if (nMaxLength > 3)
					strValue = strValue.substring(0, nMaxLength - 3) + "...";
				else
					strValue = strValue.substring(0, nMaxLength);
			}
		}
		return strValue;
	}

	/**
	 * This method determines if the original string contains the search string as the starting characters. This
	 * differs from String.startswith() by the fact the compare is done case insensitive.
	 */
	public static boolean endsWithIgnoreCase(String strOriginal, String strSearch) {
		final int nSearchLength = strSearch.length();
		return strOriginal.length() >= nSearchLength &&
				strOriginal.substring(strOriginal.length() - nSearchLength).equalsIgnoreCase(strSearch);
	}

	/**
	 * This method compares two strings. If they are both null or they are
	 * equal, this method returns true.
	 */
	public static boolean equals(String strValue1, String strValue2) {
		boolean bEquals = false;

		if (strValue1 != null)
			bEquals = strValue1.equals(strValue2);
		else
			bEquals = strValue2 == null;

		return bEquals;
	}

	public static String formatMixedCase(String strToFormat) {
		StringBuffer strReplacement = null;

		if (strToFormat != null) {
			strToFormat = strToFormat.trim();

			if (strToFormat.length() > 0) {
				strReplacement = new StringBuffer(strToFormat.length());

				boolean bUpperCase = true;
				int nExceptionPos = 0;
				char cException = '\0';
				char cLastException = '\0';

				for (int i = 0; i < strToFormat.length(); i++) {
					nExceptionPos = EXCEPTION_CHARS.indexOf(strToFormat.charAt(i));

					if (nExceptionPos >= 0) {
						cException = EXCEPTION_CHARS.charAt(nExceptionPos);

						if (bUpperCase)
							strReplacement.append(Character.toUpperCase(strToFormat.charAt(i)));
						else
							strReplacement.append(Character.toLowerCase(strToFormat.charAt(i)));

						cLastException = cException;
						bUpperCase = true;
					}
					else {
						if (bUpperCase) {
							if (cLastException == '\"' && strToFormat.charAt(i + 1) == ' ')
								strReplacement.append(Character.toLowerCase(strToFormat.charAt(i)));
							else
								strReplacement.append(Character.toUpperCase(strToFormat.charAt(i)));
						}
						else
							strReplacement.append(Character.toLowerCase(strToFormat.charAt(i)));

						bUpperCase = strReplacement.length() == 2 && strReplacement.toString().equalsIgnoreCase("Mc");
					}
				}
			}
		}

		return strReplacement != null ? strReplacement.toString() : "";
	}

	/**
	 * Defers to more appropriately named version of this method.  This
	 * method is still in place since so much code already calls it.
	 */
	public static String getFormFieldValue(String strValue) {
		return getQuotedAttributeValue(strValue);
	}

	/**
	 * This method returns the passed in string as a Java formatted source string.
	 * All embedded double-quotes, new lines, and carriage returns are escaped with
	 * backslashes.  This method is intended to be used to created properly escaped
	 * strings for use on templates with JavaScript.
	 */
	public static String getJavaString(String strValue) {
		if (strValue != null && (strValue.indexOf('"') != -1 || strValue.indexOf('\r') != -1 || strValue.indexOf('\n') != -1 ||
			strValue.indexOf('\'') != -1 || strValue.indexOf('\\') != -1)) {
			final int nLength = strValue.length();

			// Create buffer large enough to hold value, plus extra space for escaping.
			final StringBuffer strFormatValue = new StringBuffer(nLength + 16);

			for (int i = 0; i < nLength; i++) {
				final char c = strValue.charAt(i);
				if (c == '"')
					strFormatValue.append("\\\"");
				else if (c == '\r')
					strFormatValue.append("\\r");
				else if (c == '\n')
					strFormatValue.append("\\n");
				else if (c == '\'')
					strFormatValue.append("\\'");
				else if (c == '\\')
					strFormatValue.append("\\\\");
				else
					strFormatValue.append(c);
			}

			strValue = strFormatValue.toString();
		}
		else if (strValue == null)
			strValue = "";

		return strValue;
	}

	/**
	 * This method is a helper used to get a value ready for a like search. It replaces * with %
	 * and makes sure that there is a terminating %.
	 */
	public static String getLikeValue(String strOriginalValue) {
		final int nLength = strOriginalValue.length();
		final StringBuffer strBuffer = new StringBuffer(nLength + 2);

		if (nLength > 0) {
			int nStartIndex = 0;
			int nEndIndex = nLength;

			// if the value starts with a wild card then put the real wild card in the string and
			// don't get the original one
			if (strOriginalValue.charAt(0) == WILDCARD_LOCAL) {
				strBuffer.append(WILDCARD_DATABASE);
				++nStartIndex;
			}

			// if the value ends with a wild card (real or database) strip it since it will be
			// added automatically
			final char cLastChar = strOriginalValue.charAt(nLength - 1);
			if (WILDCARD_LOCAL == cLastChar || WILDCARD_DATABASE == cLastChar)
				--nEndIndex;

			if (nStartIndex < nEndIndex && nStartIndex < nLength && nEndIndex <= nLength)
				strBuffer.append(strOriginalValue.substring(nStartIndex, nEndIndex));
		}

		strBuffer.append(WILDCARD_DATABASE);

		return strBuffer.toString();
	}

	/**
	 * This method returns a markup escaped version of a source string.
	 */
	public static String getMarkupEscapedValue(String strSource) {
		if (strSource.indexOf('&') != -1 || strSource.indexOf('<') != -1 || strSource.indexOf('>') != -1) {
			final int nLength = strSource.length();

			final StringBuffer strBuffer = new StringBuffer(nLength + 16);

			for (int i = 0; i < nLength; i++) {
				final char c = strSource.charAt(i);
				if (c == '&')
					strBuffer.append("&#38;");
				else if (c == '<')
					strBuffer.append("&#60;");
				else if (c == '>')
					strBuffer.append("&#62;");
				else
					strBuffer.append(c);
			}

			strSource = strBuffer.toString();
		}

		return strSource;
	}

	/**
	 * This method is used to format and return a string that can be displayed
	 * as a markup attribute value.  The value is wrapped in single or double quotes
	 * and is appropriately escaped.
	 *
	 * @param strValue input string to be formatted.
	 * @return strFormatValue a String that has been formatted and wrapped in quotes to be displayed as an attribute value.
	 */
	public static String getQuotedAttributeValue(String strValue) {
		if (strValue != null && strValue.length() > 0) {
			final int nLength = strValue.length();

			StringBuffer strFormatValue;

			if ((strValue.indexOf('\'') >= 0 && strValue.indexOf('"') >= 0) || strValue.indexOf('\r') != -1 || strValue.indexOf('\n') != -1) {
				strFormatValue = new StringBuffer(nLength + 10);
				strFormatValue.append('"');
				for (int i = 0; i < nLength; i++) {
					final char c = strValue.charAt(i);
					if (c == '"')
						strFormatValue.append("&quot;");
					else if (c != '\r' && c != '\n')
						strFormatValue.append(c);
				}
				strFormatValue.append('"');
			}
			else if (strValue.indexOf('"') >= 0) {
				strFormatValue = new StringBuffer(nLength + 2);
				strFormatValue.append('\'');
				strFormatValue.append(strValue);
				strFormatValue.append('\'');
			}
			else {
				strFormatValue = new StringBuffer(nLength + 2);
				strFormatValue.append('"');
				strFormatValue.append(strValue);
				strFormatValue.append('"');
			}

			strValue = strFormatValue.toString();
		}
		else
			strValue = "''";

		return strValue;
	}

	/**
	 * This method is used to format and return a string that can be displayed
	 * as a markup attribute value.  The value is wrapped in single or double quotes
	 * and is appropriately escaped.
	 *
	 * @param strValue input string to be formatted.
	 * @param cWrapQuote character to wrap value in
	 * @return strFormatValue a String that has been formatted and wrapped in quotes to be displayed as an attribute value.
	 */
	public static String getQuotedAttributeValue(String strValue, char cWrapQuote) {
		if (strValue != null && strValue.length() > 0) {
			final int nLength = strValue.length();

			StringBuffer strFormatValue;

			if (strValue.indexOf(cWrapQuote) != -1 || strValue.indexOf('\r') != -1 || strValue.indexOf('\n') != -1) {
				strFormatValue = new StringBuffer(nLength + 10);

				strFormatValue.append(cWrapQuote);
				for (int i = 0; i < nLength; i++) {
					final char c = strValue.charAt(i);
					if (c == cWrapQuote)
						strFormatValue.append("&#").append(Integer.toHexString(c)).append(';');
					else if (c != '\r' && c != '\n')
						strFormatValue.append(c);
				}
				strFormatValue.append(cWrapQuote);
			}
			else {
				strFormatValue = new StringBuffer(nLength + 2);
				strFormatValue.append(cWrapQuote);
				strFormatValue.append(strValue);
				strFormatValue.append(cWrapQuote);
			}

			strValue = strFormatValue.toString();
		}
		else
			strValue = new String(new char[] {cWrapQuote, cWrapQuote});

		return strValue;
	}

	/**
	 * This method unescapes a string according to HTML escaping rules.
	 * Numeric escape sequences (decimal and hex) are handled as well as
	 * several common entity references.
	 */
	public static String htmlUnescape(String str) {
		return htmlUnescape(str, false);
	}

	/**
	 * This method unescapes a string according to HTML escaping rules.
	 * Numeric escape sequences (decimal and hex) are handled as well as
	 * several common entity references.
	 *
	 * The boolean bIgnoreNbsp, if passed in as true, will allow &nbsp; to pass by
	 * without unescaping it. This hack was necessary becuse site builder escapes
	 * quotes and such in the <span source='<ss:....>'> tags and it must be unescaped.
	 * There is one exception (<ss:value ... default="&nbsp;">) where we want to
	 * leave the &nbsp as-is.
	 */
	public static String htmlUnescape(String str, boolean bIgnoreNbsp) {
		// Make sure there's work to do before iterating over string.
		if (str != null && str.length() > 3 && str.indexOf('&') != -1 && str.indexOf(';') != -1) {
			final int nLength = str.length();
			final StringBuffer strBuffer = new StringBuffer(nLength + 16);

			for (int i = 0; i < nLength; i++) {
				final char c = str.charAt(i);

				if (c == '&') {
					final int nEndEntity = str.indexOf(';', i + 2);
					if (nEndEntity > 1 && nEndEntity - i < 10) {
						char cUnescape = '\0';

						final String strEntity = str.substring(i + 1, nEndEntity).toLowerCase();

						if (strEntity.charAt(0) == '#') {
							try {
								// Hex
								if (strEntity.charAt(1) == 'x')
									cUnescape = (char)Integer.parseInt(strEntity.substring(1), 16);
								else // Base 10
									cUnescape = (char)Integer.parseInt(strEntity.substring(1));
							}
							catch (NumberFormatException e) {
								// Eat it.
							}
						}
						else if ("quot".equals(strEntity))
							cUnescape = '"';
						else if ("amp".equals(strEntity))
							cUnescape = '&';
						else if ("lt".equals(strEntity))
							cUnescape = '<';
						else if ("gt".equals(strEntity))
							cUnescape = '>';
						else if (!bIgnoreNbsp && "nbsp".equals(strEntity))
							cUnescape = ' ';
						else if ("copy".equals(strEntity))
							cUnescape = CHAR_COPY;
						else if ("reg".equals(strEntity))
							cUnescape = CHAR_REG;

						// Add more here (or do lookup if list gets too long).

						if (cUnescape != '\0') {
							strBuffer.append(cUnescape);
							i = nEndEntity;
						}
						else
							strBuffer.append(c);
					}
					else
						strBuffer.append(c);
				}
				else
					strBuffer.append(c);
			}

			str = strBuffer.toString();
		}
		return str;
	}

	/**
	 * This method verifies that the string contains all digits.
	 *
	 * @param strValue String to be checked.
	 * @return <code>true</code> if the string contains at least 1 digit and if
	 * all other characters are digits; <code>false</code> otherwise.
	 */
	public static boolean isAllDigits(String strValue) {
		boolean bAllDigits = false;

		if (strValue != null) {
			int nLength = strValue.length();
			if (nLength > 0) {
				bAllDigits = true;
				while (bAllDigits && nLength > 0)
					bAllDigits = Character.isDigit(strValue.charAt(--nLength));
			}
		}

		return bAllDigits;
	}

	/**
	 * This method determines if the value passed in contains only ASCII characters.
	 *
	 * @param strValue the string value to be checked if ASCII
	 * @return boolean true if the string contains only ASCII character, else returns false
	 */
	public static boolean isAscii(String strValue) {
		boolean bASCII = true;

		// see if at each char is not ASCII till the end of the string or we find a violating char
		final int nLength = strValue.length();

		for (int nIndex = 0; nIndex < nLength && bASCII; nIndex++) {
			if (strValue.charAt(nIndex) > 127)
				bASCII = false;
		}

		return bASCII;
	}

	/**
	 * This method checks the existence of a string.
	 *
	 * @param strValue String containing the value to be checked.
	 * @return <code>true</code> if the string has any length.
	 */
	public static boolean isSpecified(String strValue) {
		return strValue != null && strValue.length() > 0;
	}

	/**
	 * This method checks the existence of a string.  Also, this method can be
	 * used to trim the string before checking it for a positive length.
	 *
	 * @param strValue String containing the value to be checked.
	 * @param bTrim indicates if the string should be trimmed before testing its length
	 * @return <code>true</code> if the string has any length.
	 */
	public static boolean isSpecified(String strValue, boolean bTrim) {
		if (bTrim && strValue != null)
			strValue.trim();
		
		return isSpecified(strValue);
	}
	
	public static void printFormFieldValue(PrintWriter out, String strValue) {
		out.print(getFormFieldValue(strValue));
	}

	/**
	 * This method determines if the original string contains the search string as the starting characters. This
	 * differs from String.startswith() by the fact the compare is done case insensitive.
	 */
	public static boolean startsWithIgnoreCase(String strOriginal, String strSearch) {
		return strOriginal.length() >= strSearch.length() &&
				strOriginal.substring(0, strSearch.length()).equalsIgnoreCase(strSearch);
	}

	/**
	 * This method does the equivalent of String.replace, but searches for strings instead of characters.
	 *
	 * @param strOriginalValue String containing the original source to do the looking in
	 * @param strSearch String containing what to look for
	 * @param strReplace String containing what to replace the search string with
	 */
	public static String stringReplace(String strOriginalValue, String strSearch, String strReplace) {
		if (strOriginalValue == null || strSearch == null || strReplace == null)
			throw new IllegalArgumentException("Original, search, and replace values cannot be null");

		if (!strSearch.equals(strReplace)) {
			int nIndex = strOriginalValue.indexOf(strSearch);

			// only do massaging if the search string is found at lease once
			if (nIndex >= 0) {
				// start at the end of the original string and go backwards
				nIndex = strOriginalValue.length();
				final StringBuffer strBuffer = new StringBuffer(strOriginalValue);

				// search backwards through the original string so that the indeces of the found elements
				// are the same in the original string as the modified string
				while ((nIndex = strOriginalValue.lastIndexOf(strSearch, nIndex)) >= 0) {
					// delete the search string
					strBuffer.delete(nIndex, nIndex + strSearch.length());

					// insert the replace string in its place
					strBuffer.insert(nIndex, strReplace);

					// skip over the last found string
					--nIndex;
				}

				strOriginalValue = strBuffer.toString();
			}
		}

		return strOriginalValue;
	}

	/**
	 * Strips non-alphanumeric characters from the input value.
	 */
	public static String stripNonAlphaNumerics(String str) {
		StringBuffer strClean = null;

		if (str != null) {
			final int nLength = str.length();
			for (int i = 0; i < nLength; i++) {
				final char c = str.charAt(i);
				if (!Character.isLetterOrDigit(c)) {
					// Lazy init clean buffer.
					if (strClean == null) {
						strClean = new StringBuffer(nLength);
						strClean.append(str.substring(0, i));
					}
				}
				else if (strClean != null)
					strClean.append(c);
			}
		}

		return strClean != null ? strClean.toString() : str;
	}

	/**
	 * Strips non-numeric characters from the input value.
	 */
	public static String stripNonNumerics(String str) {
		final int nLength = str.length();
		final StringBuffer strClean = new StringBuffer(nLength);

		for (int i = 0; i < nLength; i++) {
			final byte b = (byte)str.charAt(i);
			if (b > 47 && b < 58)
				strClean.append((char)b);
		}

		return strClean.toString();
	}

	/**
	 * Strips space characters from the input value.
	 */
	public static String stripSpaces(String str) {
		StringBuffer strClean = null;

		if (str != null) {
			final int nLength = str.length();
			for (int i = 0; i < nLength; i++) {
				final char c = str.charAt(i);
				if (!Character.isSpaceChar(c)) {
					// Lazy init clean buffer.
					if (strClean == null)
						strClean = new StringBuffer(nLength);

					strClean.append(c);
				}
			}
		}

		return strClean != null ? strClean.toString() : str;
	}
	
	public static String stripXssAttempt(String strUnstripped) {
		if (StringFormat.isSpecified(strUnstripped) && strUnstripped.indexOf('<') > -1)
			strUnstripped = REGEX_SCRIPT.matcher(strUnstripped).replaceAll(REPLACE_STRING);
		return strUnstripped;
	}
	
	/**
	 * Unescapes Unicode escape sequences embedded within a string. Unicode
	 * escape sequenes are of the form "\\uxxxx" where "xxxx" is the hex
	 * representation of a Unicode character.
	 */
	public static String unicodeUnescape(String str) {
		if (str != null && str.length() > 4) {
			int nIndex = str.indexOf("\\u");
			while (nIndex != -1) {
				final String strChar = str.substring(nIndex + 2, nIndex + 6);
				str = str.substring(0, nIndex) + (char)Integer.parseInt(strChar, 16) +
						str.substring(nIndex + Math.min(6, str.length()));
				nIndex = str.indexOf("\\u");
			}
		}
		return str;
	}
}
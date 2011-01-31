package com.tippingpoint.utilities;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateFormatUtils;

/**
 * This class holds simple utility classes associated with XML.
 */
public class XmlUtilities {
	/**
	 * This method is hidden to support the class being a static helper class.
	 */
	private XmlUtilities() {
	}

	/**
	 * This method returns a closing tag for the given named element.
	 */
	public static String close(final String strTagName) {
		return "</" + strTagName + ">";
	}

	/**
	 * This method returns the object in a string form.
	 * 
	 * @param value Object containing the value to convert to a string.
	 */
	public static String getValue(final Object objValue) {
		String strValue = "";

		if (objValue != null) {
			if (objValue instanceof Date) {
				strValue = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format((Date)objValue);
			}
			else {
				strValue = objValue.toString();
			}
		}

		return strValue;
	}

	/**
	 * This method returns an opening tag for the given named element.
	 */
	public static String open(final String strTagName) {
		return open(strTagName, (NameValuePair)null);
	}

	public static String open(final String strTagName, final List<NameValuePair> listAttributes) {
		final StringBuilder strBuffer = new StringBuilder();

		strBuffer.append('<');
		strBuffer.append(strTagName);

		if (listAttributes != null && !listAttributes.isEmpty()) {
			for (final NameValuePair pair : listAttributes) {
				if (pair != null && pair.getName() != null) {
					strBuffer.append(' ');
					strBuffer.append(pair.getName());
					strBuffer.append("=\"");
					if (pair.getValue() != null) {
						strBuffer.append(pair.getValue());
					}
					strBuffer.append('\"');
				}
			}
		}

		strBuffer.append('>');

		return strBuffer.toString();
	}

	/**
	 * This method returns an opening tag for the given named element.
	 */
	public static String open(final String strTagName, final NameValuePair pair) {
		final StringBuilder strBuffer = new StringBuilder();

		start(strBuffer, strTagName, pair);

		strBuffer.append('>');

		return strBuffer.toString();
	}

	/**
	 * This method returns a tag for the given named element.
	 */
	public static String tag(final String strTagName) {
		final StringBuilder strBuffer = new StringBuilder();

		start(strBuffer, strTagName);

		strBuffer.append("/>");

		return strBuffer.toString();
	}

	/**
	 * This method returns a tag for the given named element.
	 */
	public static String tag(final String strTagName, final List<NameValuePair> listAttributes) {
		final StringBuilder strBuffer = new StringBuilder();

		start(strBuffer, strTagName, listAttributes);

		strBuffer.append("/>");

		return strBuffer.toString();
	}

	/**
	 * This method returns a tag for the given named element.
	 */
	public static String tag(final String strTagName, final List<NameValuePair> listAttributes, final Object objValue) {
		final StringBuilder strBuffer = new StringBuilder();

		start(strBuffer, strTagName, listAttributes);

		if (objValue != null) {
			strBuffer.append('>');
			strBuffer.append(objValue);
			strBuffer.append(close(strTagName));
		}
		else {
			strBuffer.append("/>");
		}

		return strBuffer.toString();
	}

	/**
	 * This method returns a tag for the given named element.
	 */
	public static String tag(final String strTagName, final NameValuePair pair) {
		final StringBuilder strBuffer = new StringBuilder();

		start(strBuffer, strTagName, pair);

		strBuffer.append("/>");

		return strBuffer.toString();
	}

	/**
	 * This method returns a tag for the given named element.
	 */
	public static String tag(final String strTagName, final NameValuePair pair, final Object objValue) {
		return tag(strTagName, pair != null ? Collections.singletonList(pair) : null, objValue);
	}

	/**
	 * This method adds the tag to the passed in buffer.
	 */
	private static void start(final StringBuilder strBuffer, final String strTagName) {
		strBuffer.append('<');
		strBuffer.append(strTagName);
	}

	/**
	 * This method adds the tag to the passed in buffer.
	 */
	private static void start(final StringBuilder strBuffer, final String strTagName,
			final List<NameValuePair> listAttributes) {
		start(strBuffer, strTagName);

		if (listAttributes != null && !listAttributes.isEmpty()) {
			for (final NameValuePair pair : listAttributes) {
				if (pair != null && pair.getName() != null) {
					strBuffer.append(' ');
					strBuffer.append(pair.getName());
					strBuffer.append("=\"");
					if (pair.getValue() != null) {
						strBuffer.append(pair.getValue());
					}
					strBuffer.append('\"');
				}
			}
		}
	}

	/**
	 * This method adds the tag to the passed in buffer.
	 */
	private static void start(final StringBuilder strBuffer, final String strTagName, final NameValuePair pair) {
		start(strBuffer, strTagName, pair != null ? Collections.singletonList(pair) : null);
	}
}

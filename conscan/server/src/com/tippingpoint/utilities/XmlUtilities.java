package com.tippingpoint.utilities;

import java.util.ArrayList;
import java.util.List;

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
	public static CharSequence tag(final String strTagName, final List<NameValuePair> listAttributes) {
		final StringBuilder strBuffer = new StringBuilder();

		start(strBuffer, strTagName, listAttributes);

		strBuffer.append("/>");

		return strBuffer.toString();
	}

	/**
	 * This method returns a tag for the given named element.
	 */
	public static CharSequence tag(final String strTagName, final NameValuePair pair) {
		final StringBuilder strBuffer = new StringBuilder();

		start(strBuffer, strTagName, pair);

		strBuffer.append("/>");

		return strBuffer.toString();
	}

	/**
	 * This method adds the tag to the passed in buffer.
	 */
	private static void start(final StringBuilder strBuffer, final String strTagName,
			final List<NameValuePair> listAttributes) {
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
	}

	/**
	 * This method adds the tag to the passed in buffer.
	 */
	private static void start(final StringBuilder strBuffer, final String strTagName, final NameValuePair pair) {
		List<NameValuePair> listAttributes = null;

		if (pair != null) {
			listAttributes = new ArrayList<NameValuePair>();

			listAttributes.add(pair);
		}

		start(strBuffer, strTagName, listAttributes);
	}
}

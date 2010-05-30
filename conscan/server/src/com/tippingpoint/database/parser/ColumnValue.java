package com.tippingpoint.database.parser;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.DatabaseException;

/**
 * This class is used to collect the value for a column specified in the data XML.
 */
public class ColumnValue {
	private static Log m_log = LogFactory.getLog(ColumnValue.class);

	/** This member holds the column being collected. */
	private final Column m_column;

	/** This member holds the the value. */
	private Object m_objValue;

	/**
	 * This method constructs a new value for the column.
	 */
	public ColumnValue(final Column column) {
		m_column = column;
	}

	/**
	 * This method appends the text to the current text.
	 * 
	 * @param strText String containing the new text to be added to the existing text.
	 * @throws DatabaseException
	 */
	public void appendText(final String strText) throws DatabaseException {
		if (strText != null && strText.length() > 0) {
			if (m_objValue == null) {
				m_objValue = new String(strText);
			}
			else if (m_objValue instanceof String) {
				m_objValue = m_objValue.toString() + strText;
			}
			else if (!StringUtils.isWhitespace(strText)) {
				m_log.debug("Attempting to append a string '" + strText + "' to a value of type '" +
						m_objValue.getClass().getSimpleName() + "'");
			}
		}
	}

	/**
	 * This method returns the column being operated on.
	 */
	public Column getColumn() {
		return m_column;
	}

	/**
	 * This method returns the collected text for the column.
	 */
	public Object getValue() {
		return m_objValue;
	}

	/**
	 * This method sets the value. The previous content is removed.
	 * 
	 * @param objValue Object containing the new value.
	 */
	public void setValue(final Object objValue) {
		m_objValue = objValue;
	}
}

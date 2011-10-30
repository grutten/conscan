package com.tippingpoint.database.json;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Element;

/**
 * This class is used to convert column information to a JSON object.
 */
public class JsonColumn implements JSONAware {
	/** This member holds the column to be converted. */
	private final Column m_column;

	/**
	 * This method constructs a new instance for the given column.
	 */
	public JsonColumn(final Column column) {
		m_column = column;
	}

	/**
	 * This method gets the object as a JSON object.
	 */
	public JSONObject get() {
		return get(true);
	}

	/**
	 * This method gets the object as a JSON object.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject get(final boolean bIncludeChildren) {
		final JSONObject columm = new JSONObject();

		columm.put(Element.ATTRIBUTE_NAME, m_column.getName());
		columm.put(ColumnDefinition.ATTRIBUTE_TYPE, m_column.getType().getType());

		if (m_column instanceof ColumnDefinition) {
			final ColumnDefinition columnDefinition = (ColumnDefinition)m_column;
			if (columnDefinition.getType().hasLength()) {
				columm.put(ColumnDefinition.ATTRIBUTE_LENGTH, Integer.toString(columnDefinition.getLength()));
			}

			if (columnDefinition.isRequired()) {
				columm.put(ColumnDefinition.ATTRIBUTE_REQUIRED, Boolean.TRUE.toString());
			}

		}

		return columm;
	}

	/**
	 * This method converts the object to a string.
	 */
	@Override
	public String toJSONString() {
		final JSONObject constraint = get();

		return constraint.toJSONString();
	}
}

package com.tippingpoint.database.json;

import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.Element;

/**
 * This class is used to convert constraint information to a JSON object.
 */
public class JsonConstraint implements JSONAware {
	/** This member holds the constraint to be converted. */
	private final Constraint m_constraint;

	/**
	 * This method constructs a new instance for the given column.
	 */
	public JsonConstraint(final Constraint constraint) {
		m_constraint = constraint;
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
		final JSONObject constraint = new JSONObject();

		constraint.put(Element.ATTRIBUTE_NAME, m_constraint.getName());
		constraint.put(Constraint.ATTRIBUTE_TYPE, m_constraint.getType());

		final JSONArray columns = new JSONArray();
		final Iterator<Column> iterColumns = m_constraint.getColumns();
		if (iterColumns != null && iterColumns.hasNext()) {
			while (iterColumns.hasNext()) {
				final JsonColumn jsonColumn = new JsonColumn(iterColumns.next());

				columns.add(jsonColumn);
			}
		}

		constraint.put(ColumnDefinition.TAG_NAME, columns);

		return constraint;
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

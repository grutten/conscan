package com.tippingpoint.conscan.objects;

import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 * This class is used to convert a list of business objects to a JSON object.
 */
public class JsonBusinessObjectList implements JSONAware {
	/** This member holds the list to be converted. */
	private final List<BusinessObject> m_listObjects;

	/**
	 * This method constructs a new instance for the given column.
	 */
	public JsonBusinessObjectList(final List<BusinessObject> listObjects) {
		m_listObjects = listObjects;
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
		final JSONObject list = new JSONObject();

		if (m_listObjects != null && !m_listObjects.isEmpty()) {
			list.put("name", m_listObjects.get(0).getType());

			final JSONArray objects = new JSONArray();
			for (final BusinessObject businessObject : m_listObjects) {
				objects.add(new JsonBusinessObject(businessObject));
			}

			list.put("objects", objects);
		}

		return list;
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

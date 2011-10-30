package com.tippingpoint.conscan.objects.json;

import java.util.Date;
import java.util.Iterator;
import org.json.simple.JSONObject;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.FieldValue;
import com.tippingpoint.database.Id;

/**
 * This class is used to convert a business object to a JSON string.
 */
public class JsonBusinessObject {
	public static final String VALUE_NAME = "value";

	/** This member holds the business object being converted to JSON. */
	private final BusinessObject m_boValue;

	/**
	 * This method constructs an object for the passed in business object.
	 */
	public JsonBusinessObject(final BusinessObject boValue) {
		m_boValue = boValue;
	}

	/**
	 * This method returns the JSON object that represents the business object.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject get() {
		final JSONObject jsonObject = new JSONObject();

		jsonObject.put("type", m_boValue.getType());

		final Iterator<FieldValue> iterValues = m_boValue.getValues();
		if (iterValues != null && iterValues.hasNext()) {
			while (iterValues.hasNext()) {
				final FieldValue fieldValue = iterValues.next();
				jsonObject.put(fieldValue.getName(), getObject(fieldValue.getValue()));
			}
		}

		return jsonObject;
	}

	/**
	 * This method returns an object value suitable for the JSON value object.
	 */
	private Object getObject(Object objValue) {
		if (objValue instanceof Id) {
			objValue = objValue.toString();
		}
		else if (objValue instanceof Date) {
			objValue = objValue.toString();
		}

		return objValue;
	}
}

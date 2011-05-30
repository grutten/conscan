package com.tippingpoint.conscan.objects.json;

import java.util.Date;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.FieldValue;
import com.tippingpoint.conscan.servlet.Services;
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

		jsonObject.put(Services.ATTRIBUTE_NAME, m_boValue.getType());

		final FieldValue fvIdentifier = m_boValue.getIdentifierField();
		if (fvIdentifier != null) {
			jsonObject.put(fvIdentifier.getName(), getObject(fvIdentifier.getValue()));
		}

		final JSONArray values = new JSONArray();

		final Iterator<FieldValue> iterValues = m_boValue.getValues();
		if (iterValues != null && iterValues.hasNext()) {
			jsonObject.put(Services.TAG_FIELD, values);

			while (iterValues.hasNext()) {
				final FieldValue fieldValue = iterValues.next();
				if (fvIdentifier == null || !fieldValue.getName().equals(fvIdentifier.getName())) {
					final JSONObject jsonFieldObject = new JSONObject();

					jsonFieldObject.put(Services.ATTRIBUTE_NAME, fieldValue.getName());
					jsonFieldObject.put(VALUE_NAME, getObject(fieldValue.getValue()));

					values.add(jsonFieldObject);
				}
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

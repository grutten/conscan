package com.tippingpoint.conscan.objects;

import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.utilities.XmlUtilities;

public class JsonBusinessObject implements JSONAware {
	private static Log m_log = LogFactory.getLog(JsonBusinessObject.class);

	/** This member holds the list to be converted. */
	private final BusinessObject m_businessObject;

	/**
	 * This method constructs a new instance for the given column.
	 */
	public JsonBusinessObject(final BusinessObject businessObject) {
		m_businessObject = businessObject;
	}

	/**
	 * This method gets the object as a JSON object.
	 */
	public JSONObject get() {
		return get(false);
	}

	/**
	 * This method gets the object as a JSON object.
	 * 
	 * @throws SqlBaseException
	 */
	@SuppressWarnings("unchecked")
	public JSONObject get(final boolean bDeep) {
		final JSONObject object = new JSONObject();

		final Iterator<FieldValue> iterValues = m_businessObject.getValues();
		if (iterValues != null && iterValues.hasNext()) {
			while (iterValues.hasNext()) {
				final FieldValue fieldValue = iterValues.next();

				object.put(fieldValue.getName(), XmlUtilities.getValue(fieldValue.getValue()));
			}
		}

		if (bDeep) {
			final List<String> listRelatedNames = m_businessObject.getRelatedNames();
			if (listRelatedNames != null && !listRelatedNames.isEmpty()) {
				for (final String strRelatedName : listRelatedNames) {
					try {
						final List<BusinessObject> listRelatedObjects =
							m_businessObject.getReleatedObjects(strRelatedName);
						if (listRelatedObjects != null && !listRelatedObjects.isEmpty()) {
							final JsonBusinessObjectList list = new JsonBusinessObjectList(listRelatedObjects);

							object.put(strRelatedName, list);
						}
					}
					catch (final SqlBaseException e) {
						// FUTURE: not ideal
						m_log.error("Error retrieving business object list for " + m_businessObject, e);
					}
				}
			}
		}

		return object;
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

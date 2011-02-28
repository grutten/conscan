package com.tippingpoint.utilities;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class emulates string properties that can have a single string value or an array of string values, very similar
 * to parameters from a request object.
 */
public class StringProperties {
	/** This member holds the parameters and their values. */
	private final Map<String, String[]> m_mapParameters = new HashMap<String, String[]>();

	/**
	 * This method constructs a new instance with no properties.
	 */
	public StringProperties() {
	}

	/**
	 * This method constructs a new instance with properties found in the instance passed in.
	 * 
	 * @param properties Properties containing the valeus to load
	 */
	public StringProperties(final Properties properties) {
		load(properties);
	}

	/**
	 * This method clears all the values for the named parameter.
	 * 
	 * @param strName String containing the name of the parameter being set.
	 */
	public void clear(final String strName) {
		m_mapParameters.remove(strName);
	}

	/**
	 * This method returns the value for the named parameters.
	 * 
	 * @param strName String containing the name of the parameter being returned.
	 */
	public String getValue(final String strName) {
		return getValue(strName, null);
	}

	/**
	 * This method returns the value for the named parameters. If the value is not found, the default is returned.
	 * 
	 * @param strName String containing the name of the parameter being returned.
	 */
	public String getValue(final String strName, final String strDefault) {
		final String[] astrValues = getValues(strName);

		return astrValues != null && astrValues.length > 0 ? astrValues[0] : strDefault;
	}

	/**
	 * This method returns an array of values for the named parameter.
	 * 
	 * @param strName String containing the name of the parameter being returned.
	 */
	public String[] getValues(final String strName) {
		return m_mapParameters.get(strName);
	}

	/**
	 * This method loads this instance with the properties found in the properties instance.
	 * 
	 * @param properties Properties containing the values to load
	 */
	public void load(final Properties properties) {
		if (properties != null && !properties.isEmpty()) {
			final Enumeration<?> enumPropertyNames = properties.propertyNames();
			if (enumPropertyNames != null && enumPropertyNames.hasMoreElements()) {
				while (enumPropertyNames.hasMoreElements()) {
					final String strName = enumPropertyNames.nextElement().toString();
					final String strValue = properties.getProperty(strName);

					setValue(strName, strValue);
				}
			}
		}
	}

	/**
	 * This method sets a value in the map. If a value is already contained in the map, the new value is added as an
	 * additional value.
	 * 
	 * @param strName String containing the name of the parameter being set.
	 * @param strValue String containing the new value.
	 */
	public void setValue(final String strName, final String strValue) {
		if (m_mapParameters.containsKey(strName)) {
			final String[] astrValues = m_mapParameters.get(strName);
			final String[] astrNewValues = new String[astrValues.length + 1];

			System.arraycopy(astrValues, 0, astrNewValues, 0, astrValues.length);
			astrNewValues[astrValues.length - 1] = strValue;
		}
		else {
			m_mapParameters.put(strName, new String[] {strValue});
		}
	}
}

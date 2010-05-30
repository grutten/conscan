package com.tippingpoint.database;

import org.apache.commons.beanutils.Converter;

/**
 * This class is used to look up a column type based on the string value
 */
public class ColumnTypeConverter implements Converter {
	/**
	 * This method converts a string representation of a column type to a class instance.
	 */
	@SuppressWarnings("unchecked")
	public Object convert(final Class clsType, final Object objValue) {
		Object objConverted = null;

		if (objValue instanceof String) {
			objConverted = ColumnTypeFactory.getFactory().get((String)objValue);
		}

		if (objConverted == null) {
			throw new IllegalArgumentException("Could not interpret a column type of '" + objValue + "'");
		}

		return objConverted;
	}
}

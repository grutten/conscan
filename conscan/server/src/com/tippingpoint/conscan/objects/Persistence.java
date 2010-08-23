package com.tippingpoint.conscan.objects;

import java.util.Iterator;
import java.util.Map;
import com.tippingpoint.sql.SqlBaseException;

/**
 * This interfaces abstracts the idea that an object can be saved to a persisted state.
 */
public interface Persistence {
	/**
	 * This method returns a list of all the named elements of the business object.
	 */
	Iterator<String> getFields();

	/**
	 * This method saves the object, if necessary.
	 * 
	 * @param mapValues Map of values used to persist the object.
	 * @throws SqlBaseException
	 */
	void save(Map<String, FieldValue> mapValues) throws SqlBaseException;
}

package com.tippingpoint.conscan.objects;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.tippingpoint.sql.SqlBaseException;

/**
 * This interfaces abstracts the idea that an object can be saved to a persisted state.
 */
public interface Persistence {
	/**
	 * This method returns a collection of objects representing all of the objects of this type.
	 * 
	 * @throws SqlBaseException
	 */
	List<Map<String, FieldValue>> getAll() throws SqlBaseException;

	/**
	 * This method returns a list of all the named elements of the business object.
	 */
	Iterator<String> getFields();

	/**
	 * This method returns the name of the identifier field, if available.
	 */
	String getIdentifierName();

	/**
	 * This method saves the object, if necessary.
	 * 
	 * @param mapValues Map of values used to persist the object.
	 * @throws SqlBaseException
	 */
	void save(Map<String, FieldValue> mapValues) throws SqlBaseException;
}

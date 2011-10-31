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
	 * This method deletes the object.
	 * 
	 * @throws SqlBaseException
	 */
	void delete(Map<String, FieldValue> m_mapValues) throws SqlBaseException;

	/**
	 * This method returns a collection of objects representing all of the objects of this type.
	 * 
	 * @param listCommonValues List containing the values that will be common to all the objects..
	 * @throws SqlBaseException
	 */
	List<Map<String, FieldValue>> getAll(List<FieldValue> listCommonValues) throws SqlBaseException;

	/**
	 * This method returns a list of all the named elements of the business object.
	 */
	Iterator<String> getFields();

	/**
	 * This method returns the name of the identifier field, if available.
	 */
	String getIdentifierName();

	/**
	 * This method returns a list of business object names that are related to this object.
	 */
	List<String> getRelatedNames();

	/**
	 * This method returns a list containing the named related objects.
	 * 
	 * @param strRelatedName String containing the name of the related object
	 * @throws SqlBaseException
	 */
	List<BusinessObject> getReleatedObjects(String strRelatedName, Map<String, FieldValue> mapValues)
			throws SqlBaseException;

	/**
	 * This method saves the object, if necessary.
	 * 
	 * @param mapValues Map of values used to persist the object.
	 * @throws SqlBaseException
	 */
	void save(Map<String, FieldValue> mapValues) throws SqlBaseException;
}

package com.tippingpoint.conscan.objects;

import java.util.Iterator;
import java.util.List;
import com.tippingpoint.sql.SqlBaseException;

/**
 * This class is the base class for business objects in the system.
 */
public interface BusinessObject {
	/**
	 * This method removes the object from the persistence layer.
	 * 
	 * @throws SqlBaseException
	 */
	void delete() throws SqlBaseException;

	/**
	 * This method returns a list of all the named elements of the business object.
	 */
	Iterator<String> getFields();

	/**
	 * This method returns the identifier field value for this business object. If the object does not have an
	 * identifier, this may return null.
	 */
	FieldValue getIdentifierField();

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
	List<BusinessObject> getReleatedObjects(String strRelatedName) throws SqlBaseException;

	/**
	 * This method returns the type of business object.
	 */
	String getType();

	/**
	 * This method returns the value for the named field.
	 * 
	 * @param strName String containing the name.
	 */
	FieldValue getValue(String strName);

	/**
	 * This method returns an iterator over the value for the fields.
	 */
	Iterator<FieldValue> getValues();

	/**
	 * This method persists the object.
	 * 
	 * @throws SqlBaseException
	 */
	void save() throws SqlBaseException;

	/**
	 * This method sets the named field value.
	 * 
	 * @param strName String containing the name of the field.
	 * @param objValue Object containing the value.
	 */
	Object setValue(String strName, Object objValue);
}

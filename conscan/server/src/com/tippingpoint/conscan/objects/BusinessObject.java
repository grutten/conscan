package com.tippingpoint.conscan.objects;

import java.util.Iterator;
import com.tippingpoint.sql.SqlBaseException;

/**
 * This class is the base class for business objects in the system.
 */
public abstract class BusinessObject {
	/**
	 * This method returns a list of all the named elements of the business object.
	 */
	public abstract Iterator<String> getFields();

	/**
	 * This method returns the identifier field value for this business object. If the object does not have an
	 * identifier, this may return null.
	 */
	public abstract FieldValue getIdentifierField();

	/**
	 * This method returns the type of business object.
	 */
	public abstract String getType();

	/**
	 * This method returns the value for the named field.
	 * 
	 * @param strName String containing the name.
	 */
	public abstract FieldValue getValue(String strName);

	/**
	 * This method returns an iterator over the value for the fields.
	 */
	public abstract Iterator<FieldValue> getValues();

	/**
	 * This method persists the object.
	 * 
	 * @throws SqlBaseException
	 */
	public abstract void save() throws SqlBaseException;

	/**
	 * This method sets the named field value.
	 * 
	 * @param strName String containing the name of the field.
	 * @param objValue Object containing the value.
	 */
	public abstract Object setValue(String strName, Object objValue);
}

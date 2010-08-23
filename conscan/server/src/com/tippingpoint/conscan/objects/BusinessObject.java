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

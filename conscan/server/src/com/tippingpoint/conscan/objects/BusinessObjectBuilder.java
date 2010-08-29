package com.tippingpoint.conscan.objects;

import com.tippingpoint.sql.SqlBaseException;

/**
 * This class is used to generate new business object instances.
 */
public interface BusinessObjectBuilder {
	/**
	 * This method returns a business object instance.
	 */
	BusinessObject get();

	/**
	 * This method is used to return a business object based on the passed in value. The business object determines what
	 * that value could be.
	 * 
	 * @param objId Object containing the identifying value.
	 * @throws SqlBaseException
	 */
	BusinessObject get(Object objId) throws SqlBaseException;

	/**
	 * This method returns the name of the object being created. It must be unique in the system.
	 */
	String getName();
}

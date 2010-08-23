package com.tippingpoint.conscan.objects;

/**
 * This class is used to generate new business object instances.
 */
public interface BusinessObjectBuilder {
	/**
	 * This method returns a business object instance.
	 */
	BusinessObject get();

	/**
	 * This method returns the name of the object being created. It must be unique in the system.
	 */
	String getName();
}

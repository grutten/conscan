package com.tippingpoint.conscan.objects;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to return builder classes for generated business objects.
 */
public class BusinessObjectBuilderFactory {
	/** This member holds the singleton instance of the factory. */
	private static volatile BusinessObjectBuilderFactory m_this;

	/** This member holds the map of builders in the system. */
	private final Map<String, BusinessObjectBuilder> m_mapBuilders = new HashMap<String, BusinessObjectBuilder>();

	/**
	 * This constructor is hidden to support the singleton nature of this class.
	 */
	private BusinessObjectBuilderFactory() {
	}

	/**
	 * This method returns the named builder.
	 * 
	 * @param strName String containing the name of the builder to return.
	 */
	public BusinessObjectBuilder getBuilder(final String strName) {
		return m_mapBuilders.get(strName);
	}

	/**
	 * This method registers a builder instance.
	 */
	public BusinessObjectBuilder register(final BusinessObjectBuilder builder) {
		return m_mapBuilders.put(builder.getName(), builder);
	}

	static {
		m_this = new BusinessObjectBuilderFactory();
	}

	/**
	 * This method returns the singleton factory instance.
	 */
	public static BusinessObjectBuilderFactory get() {
		return m_this;
	}
}

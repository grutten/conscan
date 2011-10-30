package com.tippingpoint.database;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to register and retrieve column types. All column types should be retrieved from this class.
 */
public class ColumnTypeFactory {
	/** This member holds the singleton instance of the factory. */
	private static ColumnTypeFactory m_this;

	/** This member holds a map of column types to be used for the schema. */
	private final Map<String, ColumnType> m_mapTypes = new HashMap<String, ColumnType>();

	/**
	 * This method constructs a new factory. The method is hidden since the factory is intended to be a singleton.
	 */
	private ColumnTypeFactory() {
		// add the default column types
		registerType(new ColumnTypeString());
		registerType(new ColumnTypeId());
		registerType(new ColumnTypeIdReference());
		registerType(new ColumnTypeInteger());
		registerType(new ColumnTypeSmallInteger());
		registerType(new ColumnTypeDate());
		registerType(new ColumnTypeText());
		registerType(new ColumnTypeBoolean());
		registerType(new ColumnTypePassword());
		// registerType(new ColumnTypeSequence());
		// registerType(new ColumnTypeInt());
		// registerType(new ColumnTypeCurrency());
		// registerType(new ColumnTypeSearchableString());
		// registerType(new ColumnTypePercent());
		// registerType(new ColumnTypeSmallInt());
		// registerType(new ColumnTypeRate());
		// registerType(new ColumnTypeDouble());
		// registerType(new ColumnTypeSequenceRef());
		// registerType(new ColumnTypeLongText());
	}

	/**
	 * This method returns the column type for the given string representation. If the type is not found, a
	 * <code>null</code> is returned.
	 * 
	 * @param strType String containing the type of column to retrieve.
	 */
	public ColumnType get(final String strType) {
		return m_mapTypes.get(strType);
	}

	/**
	 * This method registers a column type with the factory. If the name is in use, an error is thrown.
	 */
	public void registerType(final ColumnType type) {
		if (type != null) {
			final String strType = type.getType();

			if (!m_mapTypes.containsKey(strType)) {
				m_mapTypes.put(strType, type);
			}
			else {
				throw new IllegalArgumentException(strType + " is all ready a registered column type.");
			}
		}
	}

	/**
	 * This method returns the singleton factory instance.
	 */
	public static ColumnTypeFactory getFactory() {
		if (m_this == null) {
			internalCreate();
		}

		return m_this;
	}

	/**
	 * This method creates the one instance of the factory.
	 */
	private static synchronized void internalCreate() {
		if (m_this == null) {
			m_this = new ColumnTypeFactory();
		}
	}
}

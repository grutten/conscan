package com.tippingpoint.database;

import java.util.HashMap;
import java.util.Map;

/**
 * This class constructs a constraint based on the type passed in.
 */
public final class ConstraintFactory {
	/** This member holds the singleton instance of the factory. */
	private static ConstraintFactory m_this;

	/** This member holds a map of column types to be used for the schema. */
	private final Map<String, Constraint.ConstraintFactory> m_mapTypes =
		new HashMap<String, Constraint.ConstraintFactory>();

	/**
	 * This method constructs a new factory. The method is hidden since the factory is intended to be a singleton.
	 */
	private ConstraintFactory() {
		// add the default constraint types
		registerType(new PrimaryKeyConstraint.Factory());
		registerType(new LogicalKeyConstraint.Factory());
		registerType(new Index.Factory());
		registerType(new ForeignKeyConstraint.Factory());
	}

	/**
	 * This method returns the column type for the given string representation. If the type is not found, a
	 * <code>null</code> is returned.
	 * 
	 * @param strType String containing the type of column to retrieve.
	 */
	public Constraint get(final String strType) {
		Constraint constraint = null;

		if (strType != null) {
			final Constraint.ConstraintFactory factory = m_mapTypes.get(strType.toUpperCase());
			if (factory != null) {
				constraint = factory.get();
			}
			else {
				constraint = new Constraint(strType);
			}
		}

		return constraint;
	}

	/**
	 * This method registers a column type with the factory. If the name is in use, an error is thrown.
	 */
	public void registerType(final Constraint.ConstraintFactory factory) {
		if (factory != null) {
			final String strType = factory.getType().toUpperCase();

			if (!m_mapTypes.containsKey(strType)) {
				m_mapTypes.put(strType, factory);
			}
			else {
				throw new IllegalArgumentException(strType + " is all ready a registered constraint type.");
			}
		}
	}

	/**
	 * This method returns the singleton factory instance.
	 */
	public static ConstraintFactory getFactory() {
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
			m_this = new ConstraintFactory();
		}
	}
}

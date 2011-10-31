package com.tippingpoint.sql;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to retrieve connection managers.
 */
public final class ConnectionManagerFactory {
	/** This member holds the singleton instance of the factory. */
	private static ConnectionManagerFactory m_this;

	/** This member holds a map of connection managers, indexed by name. */
	private final Map<String, ConnectionManager> m_mapManagers = new HashMap<String, ConnectionManager>();

	/** This member holds the name of the default connection manager. */
	private String m_strDefaultManagerName;

	/**
	 * This method returns the default manager.
	 */
	public ConnectionManager getDefaultManager() {
		return m_mapManagers.get(m_strDefaultManagerName);
	}

	/**
	 * This method returns the named manager.
	 */
	public ConnectionManager getManager(final String strName) {
		return m_mapManagers.get(strName);
	}

	/**
	 * This method registers a connection manager with the given name.
	 * 
	 * @param name String containing the name of the manager.
	 * @param connectionManager ConnectionManager instance initialized to generate connections.
	 */
	public void register(final String strName, final ConnectionManager connectionManager) {
		m_mapManagers.put(strName, connectionManager);

		// register the first connection manager as the default
		if (m_mapManagers.size() == 1) {
			setDefaultManager(strName);
		}
	}

	/**
	 * This method sets the name of the default manager. This can be retrieved without a name.
	 * 
	 * @param strName String containing the name of the default connection manager.
	 */
	private void setDefaultManager(final String strName) {
		m_strDefaultManagerName = strName;
	}

	/**
	 * This method returns the singleton factory instance.
	 */
	public static ConnectionManagerFactory getFactory() {
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
			m_this = new ConnectionManagerFactory();
		}
	}
}

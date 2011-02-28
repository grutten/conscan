package com.tippingpoint.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import junit.framework.TestCase;
import com.tippingpoint.database.DatabaseException;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;
import com.tippingpoint.utilities.StringProperties;

/**
 * This class is a common base class to local test cases. It handles reading the properties file used to store
 * information about the local system.
 */
public class TestCommonCase extends TestCase {
	/** This member holds properties specific to the HTTP unit testing. */
	protected StringProperties m_unitTestProperties;

	/**
	 * This method is called prior to each test case method being called.
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		if (m_unitTestProperties == null) {
			loadProperties();
			loadConnectionManager();
		}
	}

	/**
	 * This method loads the default connection manager.
	 * 
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	private void loadConnectionManager() throws SQLException, DatabaseException {
		final ConnectionManagerFactory factory = ConnectionManagerFactory.getFactory();
		if (factory.getDefaultManager() == null) {
			// set a new connection based on values found in the properties
			// determine the connection to the database
			final ConnectionManager.ConnectionSource connectionSource =
				new ConnectionManager.DriverConnectionSource(m_unitTestProperties);
			assertTrue(connectionSource.isValid());

			final ConnectionManager manager = new ConnectionManager(connectionSource);

			// register the connection manager
			factory.register("testing", manager);
		}
	}

	/**
	 * This method loads a local properties file containing testing configuration.
	 * 
	 * @throws IOException
	 */
	private void loadProperties() throws IOException {
		m_unitTestProperties = new StringProperties();

		final Properties properties = new Properties();
		final File fileProperties = new File("unittest.properties");

		try {
			properties.load(new FileReader(fileProperties));
		}
		catch (final FileNotFoundException e) {
			System.out.println("Could not find properties file '" + fileProperties.getAbsolutePath() + "'");
		}

		m_unitTestProperties.load(properties);
	}
}

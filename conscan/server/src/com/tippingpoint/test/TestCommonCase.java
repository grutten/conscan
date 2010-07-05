package com.tippingpoint.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import junit.framework.TestCase;
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

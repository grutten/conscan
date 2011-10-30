package com.tippingpoint.utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tippingpoint.conscan.servlet.Startup;

/**
 * This singleton class keeps the contents of the system's .properties file in
 * memory for quick access.
 *
 */
public class SystemProperties {
	private static final String APPLICATION_NAME = "ConScan";
	private static Log m_log = LogFactory.getLog(Startup.class);
	
	private StringProperties m_stringProperties = null;
	private static SystemProperties m_systemProperties = null;
	
	public static SystemProperties getSystemProperties() {
		if (m_systemProperties == null)
			m_systemProperties = new SystemProperties();
		
		return m_systemProperties;
	}
	
	public StringProperties getStringProperties() {
		return m_stringProperties;
	}
	
	private SystemProperties() {
		final InputStream inputStream =
			getClass().getClassLoader().getResourceAsStream(APPLICATION_NAME + ".properties");

		final Properties properties = new Properties();
		try {
			properties.load(inputStream);
		}
		catch (final FileNotFoundException e) {
			m_log.error("Exception reading application properties", e);
		}
		catch (final IOException e) {
			m_log.error("I/O Exception reading application properties", e);
		}

		m_stringProperties = new StringProperties(properties);
	}
	
}

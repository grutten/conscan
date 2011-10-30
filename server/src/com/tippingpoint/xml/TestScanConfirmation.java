package com.tippingpoint.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import com.tippingpoint.conscan.objects.TablePersistence;
import com.tippingpoint.database.DatabaseException;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.parser.Parser;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;
import com.tippingpoint.sql.SchemaComparison;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.utilities.StringProperties;

public class TestScanConfirmation extends TestCase {

	private static final String APPLICATION_NAME = "ConScan";

	private static Log m_log = LogFactory.getLog(TestScanConfirmation.class);

	public void testMain() {
		init();

		final String strConfigFile = "g:\\wkspc\\conscan\\handheld\\xml\\log20111212956.xml";
		final Data d2 = new Data(strConfigFile);
		assertNotNull(d2);
	}

	/**
	 * This method returns the properties for the application.
	 */
	private StringProperties getProperties() {
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

		return new StringProperties(properties);
	}

	private void init() {
		final StringProperties properties = getProperties();

		final ConnectionManagerFactory factory = ConnectionManagerFactory.getFactory();
		if (factory.getDefaultManager() == null) {
			// set a new connection based on values found in the properties
			// determine the connection to the database
			final ConnectionManager.ConnectionSource connectionSource =
				new ConnectionManager.DriverConnectionSource(properties);
			if (!connectionSource.isValid()) {
				m_log.error("Could not configure a database connection.");
			}

			try {
				final ConnectionManager manager = new ConnectionManager(connectionSource);

				// register the connection manager
				factory.register(APPLICATION_NAME, manager);

				final Schema schemaExisting = manager.getSchema(APPLICATION_NAME);

				final InputStream isSchema =
					getClass().getClassLoader().getResourceAsStream(APPLICATION_NAME + "Db.xml");

				final Schema schema = Parser.parse(isSchema);

				final SchemaComparison comparison = new SchemaComparison(schemaExisting, schema);

				comparison.process(manager);

				manager.setSchema(schema);

				TablePersistence.registerTables(schema);
			}
			catch (final SQLException e) {
				m_log.error("SQL Error configuring default connection.", e);
			}
			catch (final DatabaseException e) {
				m_log.error("Database error configuring default connection.", e);
			}
			catch (final IOException e) {
				m_log.error("I/O error configuring default connection.", e);
			}
			catch (final SAXException e) {
				m_log.error("SAX error configuring default connection.", e);
			}
			catch (final SqlBaseException e) {
				m_log.error("SQL error configuring default connection.", e);
			}
		}
	}
}

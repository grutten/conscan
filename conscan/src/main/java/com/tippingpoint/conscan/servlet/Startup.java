package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
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
import com.tippingpoint.utilities.SystemProperties;

/**
 * This class is used to load the application.
 */
public final class Startup extends HttpServlet {
	protected static final String METHOD_DELETE = "DELETE";
	protected static final String METHOD_GET = "GET";
	protected static final String METHOD_HEAD = "HEAD";
	protected static final String METHOD_OPTIONS = "OPTIONS";
	protected static final String METHOD_POST = "POST";
	protected static final String METHOD_PUT = "PUT";

	
	private static final String APPLICATION_NAME = "ConScan";
	private static Log m_log = LogFactory.getLog(Startup.class);

	/** This member holds the local services. */
	private static List<LocalService> m_mapServices = new ArrayList<LocalService>();

	private static final long serialVersionUID = -8868702728732992660L;

	private static final String SERVICE_SUFFIX = "service";

	/**
	 * This method is called on initialization.
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);

		final StringProperties properties = SystemProperties.getSystemProperties().getStringProperties();

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
				throw new ServletException(e);
			}
			catch (final DatabaseException e) {
				m_log.error("Database error configuring default connection.", e);
				throw new ServletException(e);
			}
			catch (final IOException e) {
				m_log.error("I/O error configuring default connection.", e);
				throw new ServletException(e);
			}
			catch (final SAXException e) {
				m_log.error("SAX error configuring default connection.", e);
				throw new ServletException(e);
			}
			catch (final SqlBaseException e) {
				m_log.error("SQL error configuring default connection.", e);
				throw new ServletException(e);
			}
		}
	}

	static {
		Startup.register(Database.class, METHOD_GET, null);
		Startup.register(Database.class, METHOD_DELETE, null);
		Startup.register(Database.class, METHOD_OPTIONS, null);
		Startup.register(Database.class, METHOD_POST, null);
		Startup.register(ScanLog.class, METHOD_GET, null);
		Startup.register(User.class, METHOD_GET, null);
	}

	/**
	 * This method registers a new web services that is available.
	 * 
	 * @param clsService Class that is providing the service.
	 * @param strMethod String containing the method parameter for the service.
	 */
	private static void register(final Class<? extends Services> clsService, final String strMethod,
			final String strDescription) {
		String strPath = clsService.getSimpleName().toLowerCase();
		if (strPath.endsWith(SERVICE_SUFFIX)) {
			strPath = strPath.substring(0, strPath.length() - SERVICE_SUFFIX.length());
		}

		m_mapServices.add(new LocalService(strPath, strMethod, strDescription));
	}

	/**
	 * This class is used to hold information about a service being offered.
	 */
	private static class LocalService {
		/** This method holds the description of the service. */
		private final String m_strDescription;

		/** This method holds the method of the service. */
		private final String m_strMethod;

		/** This method holds the path of the service. */
		private final String m_strPath;

		/**
		 * This method constructs a new local service.
		 * 
		 * @param strPath String containing the path to the service.
		 * @param strMethod String containing the method parameter for the service.
		 * @param strDescription String containing the description of the service.
		 */
		public LocalService(final String strPath, final String strMethod, final String strDescription) {
			m_strPath = strPath;
			m_strMethod = strMethod;
			m_strDescription = strDescription;
		}

		/**
		 * This method returns the description for the service.
		 */
		public String getDescription() {
			return m_strDescription;
		}

		/**
		 * This method returns the method for the service.
		 */
		public String getMethod() {
			return m_strMethod;
		}

		/**
		 * This method returns the path for the service.
		 */
		public String getPath() {
			return m_strPath;
		}
	}
}

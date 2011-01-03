package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.database.DatabaseException;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.utilities.XmlUtilities;

public final class Scanner extends Services {
	private static Log m_log = LogFactory.getLog(Scanner.class);
	private static final long serialVersionUID = -1452761419695976821L;
	private static final String TAG_CONFIGURATION = "configuration";

	/**
	 * This method executes the options command; which is used to return the activities available to the current user.
	 * 
	 * @throws IOException
	 */
	@Override
	protected void doOptions(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final String strObjects = request.getPathInfo();

		m_log.debug(request.getMethod() + ": " + strObjects);

		final PrintWriter writer = returnXml(response, HttpServletResponse.SC_OK);

		try {
			getConfiguration(writer);
		}
		catch (final SqlBaseException e) {
			processException(response, e);
		}
		catch (final DatabaseException e) {
			processException(response, e);
		}
	}

	/**
	 * This method retrieves the current configuration.
	 * 
	 * @param writer PrintWriter where the details are returned.
	 * @throws IOException
	 * @throws SqlBaseException
	 * @throws DatabaseException
	 */
	private void getConfiguration(final Writer writer) throws IOException, SqlBaseException, DatabaseException {
		writer.write(XmlUtilities.open(TAG_CONFIGURATION));
		writeObjects(writer, "location", "offender");
		writeObjects(writer, "compliance", "compliancevalue");
		writeObjects(writer, "activity", false);
		writer.write(XmlUtilities.close(TAG_CONFIGURATION));
	}
}

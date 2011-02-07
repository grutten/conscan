package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.sql.SqlBaseException;

/**
 * This class serves the <base table service> for related objects.
 */
public class BaseTableService extends Services {
	private static Log m_log = LogFactory.getLog(BaseTableService.class);
	private static final long serialVersionUID = 6661798263642526305L;

	private final String m_strTableName;

	/**
	 * This constructor sets the name of the underlying table.
	 * 
	 * @param strTableName
	 */
	protected BaseTableService(final String strTableName) {
		m_strTableName = strTableName;
	}

	/**
	 * This method executes the get command; which is used to return the contents of a <base table service>.
	 * 
	 * @throws IOException
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final String strPathInfo = request.getPathInfo();

		m_log.debug("Get: " + (strPathInfo != null ? strPathInfo : "<no path>" ));

		try {
			final List<String> listElements = getObjects(strPathInfo);
			if (listElements != null && !listElements.isEmpty()) {
				final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(m_strTableName);
				if (builder != null) {
					switch (listElements.size()) {
					case 1:
						final BusinessObject businessObject = builder.get(listElements.get(0));
						if (businessObject != null) {
							returnObject(response, businessObject);
						}
					break;

					default:
						// throw an error?
					break;
					}
				}
				else {
					returnXml(response, HttpServletResponse.SC_NO_CONTENT);
				}
			}
			else {
				final PrintWriter writer = returnXml(response, HttpServletResponse.SC_OK);
				writeObjects(writer, m_strTableName, false);
			}
		}
		catch (final SqlBaseException e) {
			m_log.error("Database error retrieving " + m_strTableName + " object.", e);
			processException(response, e);
		}
	}

	/**
	 * This method returns the passed in object.
	 * 
	 * @param response HttpServletResponse where the results are to be returned.
	 * @param businessObject BusinessObject to be returned.
	 * @throws IOException
	 * @throws SqlBaseException
	 */
	private void returnObject(final HttpServletResponse response, final BusinessObject businessObject)
			throws IOException, SqlBaseException {
		final PrintWriter writer = returnXml(response, HttpServletResponse.SC_OK);
		writeObject(writer, businessObject, false);
	}
}

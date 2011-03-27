package com.tippingpoint.conscan.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import javax.activation.MimeType;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.conscan.objects.JsonBusinessObjectList;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.utilities.NameValuePair;
import com.tippingpoint.utilities.XmlUtilities;
import com.tippingpoint.xml.Data;

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

		m_log.debug("Get: " + (strPathInfo != null ? strPathInfo : "<no path>"));

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
				writeObjects(request, response, m_strTableName);
				// final PrintWriter writer = returnXml(response, HttpServletResponse.SC_OK);
				// writeObjects(writer, m_strTableName, false);
			}
		}
		catch (final SqlBaseException e) {
			m_log.error("Database error retrieving " + m_strTableName + " object.", e);
			processException(response, e);
		}
	}

	/**
	 * This method is used to execute the post command; which is used to insert records.
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		ServletInputStream inputStream = request.getInputStream();
		final BufferedReader readerData = new BufferedReader(new InputStreamReader(inputStream));
		Data dataTippingPointServer = new Data(readerData);

		// TODO: figure out how to get the Data object to work without an instance
		// so the followiung code can be removed.
		if (dataTippingPointServer.getObjectName().equalsIgnoreCase("asdf"))
			m_log.debug("table: " + dataTippingPointServer.getObjectName());
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

	/**
	 * This method returns a list of objects of the specified type.
	 * 
	 * @param request
	 * @param response
	 * @param strObjectName String containing the name of the object to write.
	 * @throws IOException
	 * @throws SqlBaseException
	 */
	private void writeObjects(final HttpServletRequest request, final HttpServletResponse response,
			final String strObjectName) throws IOException, SqlBaseException {
		final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(strObjectName);
		if (builder != null) {
			final List<BusinessObject> listObjects = builder.getAll();
			if (listObjects != null && !listObjects.isEmpty()) {
				final List<MimeType> listAccepts = getAccepts(request);

				for (final MimeType mimeType : listAccepts) {
					if (MIME_JSON.match(mimeType)) {
						response.setStatus(HttpServletResponse.SC_OK);
						response.setContentType(MIME_JSON.toString());

						final PrintWriter out = response.getWriter();

						final JsonBusinessObjectList jsonObjects = new JsonBusinessObjectList(listObjects);

						jsonObjects.get().writeJSONString(out);
						break;
					}
					else if (MIME_XML.match(mimeType)) {
						final PrintWriter out = returnXml(response, HttpServletResponse.SC_OK);
						out.write(XmlUtilities.open(TAG_LIST, new NameValuePair(ATTRIBUTE_NAME, listObjects.get(0)
								.getType())));

						for (final BusinessObject businessObject : listObjects) {
							writeObject(out, businessObject, false);
						}

						out.write(XmlUtilities.close(TAG_LIST));
						break;
					}
				}
			}
		}
	}
}

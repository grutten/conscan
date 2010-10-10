package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.FieldValue;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.utilities.NameValuePair;
import com.tippingpoint.utilities.XmlUtilities;

/**
 * This class is the base class for service based servlets.
 */
public abstract class Services extends HttpServlet {
	protected static final String ATTRIBUTE_NAME = "name";

	protected static final String METHOD_DELETE = "DELETE";
	protected static final String METHOD_GET = "GET";
	protected static final String METHOD_HEAD = "HEAD";
	protected static final String METHOD_OPTIONS = "OPTIONS";
	protected static final String METHOD_POST = "POST";
	protected static final String METHOD_PUT = "PUT";

	protected static final String TAG_FIELD = "field";
	protected static final String TAG_LIST = "list";
	protected static final String TAG_OBJECT = "object";

	private static final long serialVersionUID = -5482024580102875533L;

	/**
	 * This method returns and XML string representing the exception.
	 * 
	 * @throws IOException
	 */
	protected void processException(final HttpServletResponse response, Throwable t) throws IOException {
		final PrintWriter writer = returnXml(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

		writer.append("<errors>");

		while (t != null) {
			writer.append("<error>");
			writer.append("<class>").append(StringEscapeUtils.escapeXml(t.getClass().toString())).append("</class>");
			writer.append("<message>").append(StringEscapeUtils.escapeXml(t.getMessage())).append("</message>");
			writer.append("<trace>");
			t.printStackTrace(writer);
			writer.append("</trace>");
			writer.append("</error>");

			t = t.getCause();
		}
		writer.append("</errors>");
	}

	/**
	 * This method prepares the response for returning XML.
	 * 
	 * @param response HttpServletResponse that is being prepared.
	 * @param nStatus int containing the default status.
	 * @throws IOException
	 */
	protected PrintWriter returnXml(final HttpServletResponse response, final int nStatus) throws IOException {
		response.setStatus(nStatus);
		response.setContentType("text/xml");

		return response.getWriter();
	}

	/**
	 * This method writes the business object to the writer.
	 * 
	 * @param writer Writer used for writing out the XML.
	 * @param businessObject BusinessObject which is being written to the XML.
	 * @param bDeep boolean indicating if related objects should be retrieved at the same time.
	 * @throws IOException
	 * @throws SqlBaseException
	 */
	protected void writeObject(final Writer writer, final BusinessObject businessObject, final boolean bDeep)
			throws IOException, SqlBaseException {
		final List<NameValuePair> listAttributes = new ArrayList<NameValuePair>();

		listAttributes.add(new NameValuePair(ATTRIBUTE_NAME, businessObject.getType()));

		final FieldValue fvIdentifier = businessObject.getIdentifierField();
		if (fvIdentifier != null) {
			listAttributes
					.add(new NameValuePair(fvIdentifier.getName(), XmlUtilities.getValue(fvIdentifier.getValue())));
		}

		writer.write(XmlUtilities.open(TAG_OBJECT, listAttributes));

		final Iterator<FieldValue> iterValues = businessObject.getValues();
		if (iterValues != null && iterValues.hasNext()) {
			while (iterValues.hasNext()) {
				final FieldValue fieldValue = iterValues.next();
				if (fvIdentifier == null || !fieldValue.getName().equals(fvIdentifier.getName())) {
					writer.write(XmlUtilities.tag(TAG_FIELD, new NameValuePair(ATTRIBUTE_NAME, fieldValue.getName()),
							XmlUtilities.getValue(fieldValue.getValue())));
				}
			}
		}

		if (bDeep) {
			final List<String> listRelatedNames = businessObject.getRelatedNames();
			if (listRelatedNames != null && !listRelatedNames.isEmpty()) {
				for (final String strRelatedName : listRelatedNames) {
					final List<BusinessObject> listRelatedObjects = businessObject.getReleatedObjects(strRelatedName);
					if (listRelatedObjects != null && !listRelatedObjects.isEmpty()) {
						writer.write(XmlUtilities.open(TAG_LIST, new NameValuePair(ATTRIBUTE_NAME, strRelatedName)));
						for (final BusinessObject businessRelatedObject : listRelatedObjects) {
							writeObject(writer, businessRelatedObject, false);
						}

						writer.write(XmlUtilities.close(TAG_LIST));
					}
				}
			}
		}

		writer.write(XmlUtilities.close(TAG_OBJECT));
	}
}

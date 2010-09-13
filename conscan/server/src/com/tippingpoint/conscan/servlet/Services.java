package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.FieldValue;
import com.tippingpoint.utilities.NameValuePair;
import com.tippingpoint.utilities.XmlUtilities;

/**
 * This class is the base class for service based servlets.
 */
public abstract class Services extends HttpServlet {
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
	 * @throws IOException
	 */
	protected void writeObject(final Writer writer, final BusinessObject businessObject) throws IOException {
		NameValuePair pair = null;
		final FieldValue fvIdentifier = businessObject.getIdentifierField();
		if (fvIdentifier != null) {
			pair = new NameValuePair(fvIdentifier.getName(), XmlUtilities.getValue(fvIdentifier.getValue()));
		}

		writer.write(XmlUtilities.open(businessObject.getType(), pair));

		final Iterator<FieldValue> iterValues = businessObject.getValues();
		if (iterValues != null && iterValues.hasNext()) {
			while (iterValues.hasNext()) {
				final FieldValue fieldValue = iterValues.next();
				if (!fieldValue.getName().equals(pair.getName())) {
					writer.write(XmlUtilities.tag(fieldValue.getName(), null, XmlUtilities.getValue(fieldValue
							.getValue())));
				}
			}
		}

		writer.write(XmlUtilities.close(businessObject.getType()));
	}
}

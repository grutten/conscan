package com.tippingpoint.conscan.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.conscan.objects.FieldValue;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.utilities.NameValuePair;
import com.tippingpoint.utilities.XmlUtilities;

/**
 * This class is the base class for service based servlets.
 */
public abstract class Services extends HttpServlet {
	protected static MimeType MIME_ALL;
	protected static MimeType MIME_JSON;
	protected static MimeType MIME_XML;

	private static Log m_log = LogFactory.getLog(Services.class);

	private static final long serialVersionUID = -5482024580102875533L;

	/**
	 * This method returns the list of accept types from the request.
	 */
	protected List<MimeType> getAccepts(final HttpServletRequest request) {
		final List<MimeType> listAccepts = new ArrayList<MimeType>();
		String strAccepts = request.getHeader("Accept");
		if (strAccepts == null || strAccepts.length() == 0) {
			strAccepts = "*/*";
		}

		final StringTokenizer tokenizer = new StringTokenizer(strAccepts, ",'");
		while (tokenizer.hasMoreTokens()) {
			final String strToken = tokenizer.nextToken();
			if (StringUtils.isNotBlank(strToken)) {
				try {
					final MimeType mimeType = new MimeType(strToken);

					m_log.debug("Request has accept type of '" + mimeType.toString() + "'");

					// insert at the start so that the last one is used first
					listAccepts.add(mimeType);
				}
				catch (final MimeTypeParseException e) {
					m_log.error("Request has an invalid accept type of '" + strToken + "'", e);
				}
			}
		}

		if (listAccepts.size() > 1) {
			Collections.sort(listAccepts, new MimeTypeComparator());
		}

		return listAccepts;
	}


	/**
	 * This method breaks down the string used to identify the object.
	 * 
	 * @param strPathInfo String containing the path information.
	 */
	protected List<String> getObjects(final String strPathInfo) {
		final List<String> listElements = new ArrayList<String>();

		// convert the path information to an array of strings
		if (StringUtils.isNotBlank(strPathInfo)) {
			final StringTokenizer tokenizer = new StringTokenizer(strPathInfo, "/");
			while (tokenizer.hasMoreTokens()) {
				final String strElement = StringUtils.trimToNull(tokenizer.nextToken());
				if (strElement != null) {
					listElements.add(strElement);
				}
			}
		}

		return listElements;
	}

	/**
	 * This method parses the payload as parameters.
	 * 
	 * @param request HttpServletRequest representing the request.
	 * @throws IOException
	 */
	protected Map<String, String> getParameterMap(final HttpServletRequest request) throws IOException {
		final Map<String, String> mapParameters = new HashMap<String, String>();

		final BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));

		String strLine = null;

		do {
			strLine = reader.readLine();
			if (strLine != null) {
				parseLine(strLine, mapParameters);
			}
		} while (strLine != null);

		return mapParameters;
	}

	/**
	 * This method inserts a single record into the table.
	 * 
	 * @param table Table which is the target of the insert.
	 * @param request HttpServletRequest which is making the request.
	 * @param response HttpServletResponse where the results are to be returned.
	 * @throws SQLException
	 * @throws SqlBaseException
	 */
	protected void insertTable(final Table table, final HttpServletRequest request, final HttpServletResponse response)
			throws SqlBaseException, SQLException {
		final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(table.getName());

		final BusinessObject object = builder.get();

		final Iterator<String> iterNames = object.getFields();
		if (iterNames != null && iterNames.hasNext()) {
			while (iterNames.hasNext()) {
				final String strName = iterNames.next();
				object.setValue(strName, StringUtils.trimToNull(request.getParameter(strName)));
			}

			object.save();
		}

		response.setStatus(HttpServletResponse.SC_OK);
	}

	/**
	 * This method parses a set of name value pairs from the line. The line is assumed to be in the form:
	 * name1=value1&name2=value2...
	 * 
	 * @param strLine String containing the name value pairs.
	 * @param mapParameters Map where the parameters will be stored.
	 */
	protected void parseLine(final String strLine, final Map<String, String> mapParameters) {
		if (strLine != null && strLine.length() > 0) {
			final StringTokenizer tokenizer = new StringTokenizer(strLine, "&");
			while (tokenizer.hasMoreTokens()) {
				final String strToken = tokenizer.nextToken();
				if (StringUtils.isNotEmpty(strToken)) {
					final int nIndex = strToken.indexOf('=');
					if (nIndex > -1) {
						final String strName = strToken.substring(0, nIndex);
						String strValue = null;

						if (nIndex < strToken.length() - 1) {
							try {
								strValue = URLDecoder.decode(strToken.substring(nIndex + 1), "UTF-8");
							}
							catch (final UnsupportedEncodingException e) {
								// should never happen
							}
						}

						mapParameters.put(strName, strValue);
					}
				}
			}
		}
	}

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
		response.setContentType(MIME_XML.toString());

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
		if (businessObject != null) {
			final List<NameValuePair> listAttributes = new ArrayList<NameValuePair>();

			listAttributes.add(new NameValuePair(XmlTags.ATTRIBUTE_NAME, businessObject.getType()));

			final FieldValue fvIdentifier = businessObject.getIdentifierField();
			if (fvIdentifier != null) {
				listAttributes.add(new NameValuePair(fvIdentifier.getName(), XmlUtilities.getValue(fvIdentifier
						.getValue())));
			}

			writer.write(XmlUtilities.open(XmlTags.TAG_OBJECT, listAttributes));

			final Iterator<FieldValue> iterValues = businessObject.getValues();
			if (iterValues != null && iterValues.hasNext()) {
				while (iterValues.hasNext()) {
					final FieldValue fieldValue = iterValues.next();
					if (fvIdentifier == null || !fieldValue.getName().equals(fvIdentifier.getName())) {
						writer.write(XmlUtilities.tag(XmlTags.TAG_FIELD,
								new NameValuePair(XmlTags.ATTRIBUTE_NAME, fieldValue.getName()),
								XmlUtilities.getValue(fieldValue.getValue())));
					}
				}
			}

			if (bDeep) {
				final List<String> listRelatedNames = businessObject.getRelatedNames();
				if (listRelatedNames != null && !listRelatedNames.isEmpty()) {
					for (final String strRelatedName : listRelatedNames) {
						final List<BusinessObject> listRelatedObjects =
							businessObject.getReleatedObjects(strRelatedName);
						if (listRelatedObjects != null && !listRelatedObjects.isEmpty()) {
							writer.write(XmlUtilities.open(XmlTags.TAG_LIST, new NameValuePair(XmlTags.ATTRIBUTE_NAME, strRelatedName)));
							for (final BusinessObject businessRelatedObject : listRelatedObjects) {
								writeObject(writer, businessRelatedObject, false);
							}

							writer.write(XmlUtilities.close(XmlTags.TAG_LIST));
						}
					}
				}
			}

			writer.write(XmlUtilities.close(XmlTags.TAG_OBJECT));
		}
		else {
			writer.write(XmlUtilities.tag(XmlTags.TAG_OBJECT));
		}
	}

	/**
	 * This method writes the business object to the writer. Additionally, the named child are written.
	 * 
	 * @param writer Writer used for writing out the XML.
	 * @param businessObject BusinessObject which is being written to the XML.
	 * @param strObjectChildName String containing the name of the object to write.
	 * @throws IOException
	 * @throws SqlBaseException
	 */
	protected void writeObject(final Writer writer, final BusinessObject businessObject, final String strObjectChildName)
			throws IOException, SqlBaseException {
		final List<NameValuePair> listAttributes = new ArrayList<NameValuePair>();

		listAttributes.add(new NameValuePair(XmlTags.ATTRIBUTE_NAME, businessObject.getType()));

		final FieldValue fvIdentifier = businessObject.getIdentifierField();
		if (fvIdentifier != null) {
			listAttributes
					.add(new NameValuePair(fvIdentifier.getName(), XmlUtilities.getValue(fvIdentifier.getValue())));
		}

		writer.write(XmlUtilities.open(XmlTags.TAG_OBJECT, listAttributes));

		final Iterator<FieldValue> iterValues = businessObject.getValues();
		if (iterValues != null && iterValues.hasNext()) {
			while (iterValues.hasNext()) {
				final FieldValue fieldValue = iterValues.next();
				if (fvIdentifier == null || !fieldValue.getName().equals(fvIdentifier.getName())) {
					writer.write(XmlUtilities.tag(XmlTags.TAG_FIELD, new NameValuePair(XmlTags.ATTRIBUTE_NAME, fieldValue.getName()),
							XmlUtilities.getValue(fieldValue.getValue())));
				}
			}
		}

		if (StringUtils.isNotBlank(strObjectChildName)) {
			final List<BusinessObject> listRelatedObjects = businessObject.getReleatedObjects(strObjectChildName);
			if (listRelatedObjects != null && !listRelatedObjects.isEmpty()) {
				writer.write(XmlUtilities.open(XmlTags.TAG_LIST, new NameValuePair(XmlTags.ATTRIBUTE_NAME, listRelatedObjects.get(0)
						.getType())));
				for (final BusinessObject businessRelatedObject : listRelatedObjects) {
					writeObject(writer, businessRelatedObject, false);
				}

				writer.write(XmlUtilities.close(XmlTags.TAG_LIST));
			}
		}

		writer.write(XmlUtilities.close(XmlTags.TAG_OBJECT));
	}

	/**
	 * This method writes the name objects to the writer.
	 * 
	 * @param writer Writer used for writing out the XML.
	 * @param strObjectName String containing the name of the object to write.
	 * @param bDeep boolean indicating if related objects should be retrieved at the same time.
	 * @throws SqlBaseException
	 * @throws IOException
	 */
	protected void writeObjects(final Writer writer, final String strObjectName, final boolean bDeep)
			throws SqlBaseException, IOException {
		final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(strObjectName);
		if (builder != null) {
			final List<BusinessObject> listObjects = builder.getAll();
			if (listObjects != null && !listObjects.isEmpty()) {
				writer.write(XmlUtilities.open(XmlTags.TAG_LIST,
						new NameValuePair(XmlTags.ATTRIBUTE_NAME, listObjects.get(0).getType())));

				for (final BusinessObject businessObject : listObjects) {
					writeObject(writer, businessObject, bDeep);
				}

				writer.write(XmlUtilities.close(XmlTags.TAG_LIST));
			}
		}
	}

	/**
	 * This method writes the name objects to the writer. Additionally, the named children are written.
	 * 
	 * @param writer Writer used for writing out the XML.
	 * @param strObjectName String containing the name of the object to write.
	 * @param strObjectChildName String containing the name of the object to write.
	 * @throws SqlBaseException
	 * @throws IOException
	 */
	protected void writeObjects(final Writer writer, final String strObjectName, final String strObjectChildName)
			throws SqlBaseException, IOException {
		final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(strObjectName);
		if (builder != null) {
			final List<BusinessObject> listObjects = builder.getAll();
			if (listObjects != null && !listObjects.isEmpty()) {
				writer.write(XmlUtilities.open(XmlTags.TAG_LIST,
						new NameValuePair(XmlTags.ATTRIBUTE_NAME, listObjects.get(0).getType())));

				for (final BusinessObject businessObject : listObjects) {
					writeObject(writer, businessObject, strObjectChildName);
				}

				writer.write(XmlUtilities.close(XmlTags.TAG_LIST));
			}
		}
	}

	static {
		try {
			MIME_ALL = new MimeType("*/*");
			MIME_JSON = new MimeType("application/json");
			MIME_XML = new MimeType("text/xml");
		}
		catch (final MimeTypeParseException e) {
			// should never happen since the type is a constant
			m_log.error("Error parsing JSON mime type.", e);
		}
	}

	/**
	 * This class is used to sort Mime types. In general, the non-specific references are moved to the end of the list.
	 */
	private static class MimeTypeComparator implements Comparator<MimeType> {
		@Override
		public int compare(final MimeType mimeType1, final MimeType mimeType2) {
			int nCompare = -1; // assume the current order

			if (mimeType1.getPrimaryType().equals(mimeType2.getPrimaryType())) {
				if (mimeType1.getSubType().equals(mimeType2.getSubType())) {
					nCompare = 0;
				}
				else if ("*".equals(mimeType1.getSubType())) {
					nCompare = 1; // move 2 to before 1 since it represents all types
				}
			}
			else if ("*".equals(mimeType1.getPrimaryType())) {
				nCompare = 1; // move 2 to before 1 since it represents all types
			}

			return nCompare;
		}
	}
}

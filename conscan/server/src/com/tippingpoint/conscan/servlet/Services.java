package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.DatabaseElementException;
import com.tippingpoint.database.Element;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.sql.SqlExecutionException;
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
	protected static MimeType MIME_ALL;
	protected static MimeType MIME_JSON;
	protected static MimeType MIME_XML;

	protected static final String TAG_FIELD = "field";
	protected static final String TAG_LIST = "list";
	protected static final String TAG_OBJECT = "object";

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
	 * @param strObjects String containing the path information.
	 * @throws SQLException
	 * @throws DatabaseElementException
	 * @throws SqlExecutionException
	 */
	protected List<Element> getElements(final String strObjects) throws DatabaseElementException, SQLException,
			SqlExecutionException {
		final List<Element> listElements = new ArrayList<Element>();

		// convert the path string of type 'table/column' to an array of strings
		if (StringUtils.isNotBlank(strObjects)) {
			final List<String> listObjects = new ArrayList<String>();
			final StringTokenizer tokenizer = new StringTokenizer(strObjects, "/");
			while (tokenizer.hasMoreTokens()) {
				final String strObject = StringUtils.trimToNull(tokenizer.nextToken());
				if (strObject != null) {
					listObjects.add(strObject);
				}
			}

			// if strings were specified, then convert to elements
			if (listObjects.size() > 0) {
				final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
				final Schema schema = manager.getSchema(manager.getConnectionSource().getSchema());

				final Table table = schema.getTable(listObjects.get(0));
				if (table != null) {
					listElements.add(table);

					// if there are more objects, then find the column or constraint
					if (listObjects.size() > 1) {
						final ColumnDefinition column = table.getColumn(listObjects.get(1));
						if (column != null) {
							listElements.add(column);
						}
						else {
							final Constraint constraint = table.getConstraint(listObjects.get(1));
							if (constraint != null) {
								listElements.add(constraint);
							}
						}
					}
				}
			}
		}

		return listElements;
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

			listAttributes.add(new NameValuePair(ATTRIBUTE_NAME, businessObject.getType()));

			final FieldValue fvIdentifier = businessObject.getIdentifierField();
			if (fvIdentifier != null) {
				listAttributes.add(new NameValuePair(fvIdentifier.getName(), XmlUtilities.getValue(fvIdentifier
						.getValue())));
			}

			writer.write(XmlUtilities.open(TAG_OBJECT, listAttributes));

			final Iterator<FieldValue> iterValues = businessObject.getValues();
			if (iterValues != null && iterValues.hasNext()) {
				while (iterValues.hasNext()) {
					final FieldValue fieldValue = iterValues.next();
					if (fvIdentifier == null || !fieldValue.getName().equals(fvIdentifier.getName())) {
						writer.write(XmlUtilities.tag(TAG_FIELD,
								new NameValuePair(ATTRIBUTE_NAME, fieldValue.getName()), XmlUtilities
										.getValue(fieldValue.getValue())));
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
							writer
									.write(XmlUtilities.open(TAG_LIST,
											new NameValuePair(ATTRIBUTE_NAME, strRelatedName)));
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
		else {
			writer.write(XmlUtilities.tag(TAG_OBJECT));
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

		if (StringUtils.isNotBlank(strObjectChildName)) {
			final List<BusinessObject> listRelatedObjects = businessObject.getReleatedObjects(strObjectChildName);
			if (listRelatedObjects != null && !listRelatedObjects.isEmpty()) {
				writer.write(XmlUtilities.open(TAG_LIST, new NameValuePair(ATTRIBUTE_NAME, listRelatedObjects.get(0)
						.getType())));
				for (final BusinessObject businessRelatedObject : listRelatedObjects) {
					writeObject(writer, businessRelatedObject, false);
				}

				writer.write(XmlUtilities.close(TAG_LIST));
			}
		}

		writer.write(XmlUtilities.close(TAG_OBJECT));
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
				writer.write(XmlUtilities.open(TAG_LIST,
						new NameValuePair(ATTRIBUTE_NAME, listObjects.get(0).getType())));

				for (final BusinessObject businessObject : listObjects) {
					writeObject(writer, businessObject, bDeep);
				}

				writer.write(XmlUtilities.close(TAG_LIST));
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
				writer.write(XmlUtilities.open(TAG_LIST,
						new NameValuePair(ATTRIBUTE_NAME, listObjects.get(0).getType())));

				for (final BusinessObject businessObject : listObjects) {
					writeObject(writer, businessObject, strObjectChildName);
				}

				writer.write(XmlUtilities.close(TAG_LIST));
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

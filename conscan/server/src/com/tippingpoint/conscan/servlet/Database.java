package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.activation.MimeType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.DatabaseElementException;
import com.tippingpoint.database.DatabaseException;
import com.tippingpoint.database.Element;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.database.json.JsonTable;
import com.tippingpoint.database.parser.Importer;
import com.tippingpoint.database.parser.Parser;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;
import com.tippingpoint.sql.SqlAlter;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.sql.SqlBuilderException;
import com.tippingpoint.sql.SqlDrop;
import com.tippingpoint.sql.SqlExecutionException;
import com.tippingpoint.sql.SqlManagerException;
import com.tippingpoint.sql.SqlQuery;
import com.tippingpoint.sql.base.SqlExecution;
import com.tippingpoint.sql.base.SqlManager;
import com.tippingpoint.utilities.NameValuePair;
import com.tippingpoint.utilities.XmlUtilities;

public final class Database extends Services {
	private static Log m_log = LogFactory.getLog(Database.class);

	private static final long serialVersionUID = 1389375741587926242L;

	/**
	 * This method executes the delete command.
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final String strObjects = request.getPathInfo();

		m_log.debug("Delete: " + strObjects);

		try {
			final List<Element> listElements = getElements(strObjects);
			if (listElements != null && !listElements.isEmpty()) {
				final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
				switch (listElements.size()) {
				case 1:
					final SqlDrop sqlDrop = new SqlDrop((Table)listElements.get(0));

					manager.getSqlManager().executeUpdate(sqlDrop);
				break;

				case 2:
					final SqlAlter sqlAlter = new SqlAlter((Table)listElements.get(0));

					final Element element = listElements.get(1);
					if (element instanceof Constraint) {
						sqlAlter.drop((Constraint)element);
					}

					manager.getSqlManager().executeUpdate(sqlAlter);
				break;

				default:
					// FUTURE: possibly alter to drop columns
				break;
				}
			}
			else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, strObjects);
			}
		}
		catch (final DatabaseException e) {
			m_log.error("Database error deleting table.", e);
			processException(response, e);
		}
		catch (final SqlBaseException e) {
			m_log.error("Error deleting table.", e);
			processException(response, e);
		}
		catch (final SQLException e) {
			m_log.error("SQL error deleting table.", e);
			processException(response, e);
		}
	}

	/**
	 * This method executes the get command; which is used to return the contents from the database.
	 * 
	 * database/<table> - return the contents of the named table
	 * database/<table>?<column>=<value> - return the contents of the named table for the value in the specified column
	 * 
	 * @throws IOException
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final String strObjects = request.getPathInfo();

		m_log.debug("Get: " + strObjects);

		try {
			final List<Element> listElements = getElements(strObjects);
			switch (listElements.size()) {
			case 1:
				final Element element = listElements.get(0);
				if (element instanceof Table) {
					retrieveTable((Table)element, response);
				}

			default:
				// FUTURE: ???
			break;
			}
		}
		catch (final DatabaseException e) {
			m_log.error("Database error retrieving table information.", e);
			processException(response, e);
		}
		catch (final SQLException e) {
			m_log.error("SQL error retrieving table information.", e);
			processException(response, e);
		}
		catch (final SqlManagerException e) {
			m_log.error("SQL manager error retrieving table information.", e);
			processException(response, e);
		}
		catch (final SqlBuilderException e) {
			m_log.error("SQL builder error retrieving table information.", e);
			processException(response, e);
		}
		catch (final SqlExecutionException e) {
			m_log.error("SQL execution error retrieving table information.", e);
			processException(response, e);
		}
	}

	/**
	 * This method executes the options command; which is used to return the definitions from the database.
	 * 
	 * @throws IOException
	 */
	@Override
	protected void doOptions(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final String strObjects = request.getPathInfo();

		m_log.debug("Options: " + strObjects);

		try {
			final List<Element> listElements = getElements(strObjects);
			switch (listElements.size()) {
			case 0:
				final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
				processSchema(manager.getSchema(), response);
			break;

			case 1:
				final Element element = listElements.get(0);
				if (element instanceof Table) {
					processTable(request, (Table)element, response);
				}

			default:
				// FUTURE: possibly alter to drop columns
			break;
			}
		}
		catch (final DatabaseException e) {
			m_log.error("Database error retrieving table definition.", e);
			processException(response, e);
		}
		catch (final SqlExecutionException e) {
			m_log.error("Database error retrieving table definition.", e);
			processException(response, e);
		}
		catch (final SQLException e) {
			m_log.error("SQL error deleting table.", e);
			processException(response, e);
		}
	}

	/**
	 * This method performs the post action.
	 * 
	 * @throws IOException
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		// check that we have a file upload request
		final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (isMultipart) {
			// create a new file upload handler
			final ServletFileUpload upload = new ServletFileUpload();

			// parse the request
			try {
				final FileItemIterator iter = upload.getItemIterator(request);
				while (iter.hasNext()) {
					final FileItemStream fileItemStream = iter.next();
					final String strName = fileItemStream.getFieldName();
					final InputStream stream = fileItemStream.openStream();
					if (fileItemStream.isFormField()) {
						System.out.println("Form field " + strName + " with value " + Streams.asString(stream) +
								" detected.");
					}
					else {
						System.out.println("File field " + strName + " with file name " + fileItemStream.getName() +
								" detected.");

						final Reader readerData = new InputStreamReader(stream);
						Parser.parseImport(readerData, new Importer(ConnectionManagerFactory.getFactory()
								.getDefaultManager().getSchema()));
					}
				}
			}
			catch (final FileUploadException e) {
				m_log.error("File upload error importing data.", e);
				processException(response, e);
			}
			catch (final IOException e) {
				m_log.error("I/O error importing data.", e);
				processException(response, e);
			}
			catch (final SAXException e) {
				m_log.error("SAX error importing data.", e);
				processException(response, e);
			}
		}
		else {
			// assume it is just an insert into a table
			final String strObjects = request.getPathInfo();

			m_log.debug("Post: " + strObjects);

			try {
				final List<Element> listElements = getElements(strObjects);
				switch (listElements.size()) {
				case 1:
					final Element element = listElements.get(0);
					if (element instanceof Table) {
						insertTable((Table)element, request, response);
					}
				default:
				break;
				}
			}
			catch (final DatabaseException e) {
				m_log.error("Database error inserting row into table.", e);
				processException(response, e);
			}
			catch (final SqlExecutionException e) {
				m_log.error("Database error inserting row into table.", e);
				processException(response, e);
			}
			catch (final SQLException e) {
				m_log.error("SQL error inserting row into table.", e);
				processException(response, e);
			}
			catch (final SqlBaseException e) {
				m_log.error("SQL error inserting row into table.", e);
				processException(response, e);
			}
		}
	}

	/**
	 * This method breaks down the string used to identify the object.
	 * 
	 * @param strObjects String containing the path information.
	 * @throws SQLException
	 * @throws DatabaseElementException
	 * @throws SqlExecutionException
	 */
	private List<Element> getElements(final String strObjects) throws DatabaseElementException, SQLException,
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
	 * This method inserts a single record into the table.
	 * 
	 * @param table Table which is the target of the insert.
	 * @param request HttpServletRequest which is making the request.
	 * @param response HttpServletResponse where the results are to be returned.
	 * @throws SQLException
	 * @throws SqlBaseException
	 */
	private void insertTable(final Table table, final HttpServletRequest request, final HttpServletResponse response)
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
	 * This method processes returning all the tables in the schema.
	 * 
	 * @param response HttpServletResponse where the results are to be returned.
	 * @throws IOException
	 */
	private void processSchema(final Schema schema, final HttpServletResponse response) throws IOException {
		final PrintWriter writer = returnXml(response, HttpServletResponse.SC_OK);

		writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

		writer.append(XmlUtilities.open(Schema.TAG_NAME, new NameValuePair(Element.ATTRIBUTE_NAME, schema.getName())));

		final Iterator<Table> iterTables = schema.getTables();
		if (iterTables != null && iterTables.hasNext()) {
			while (iterTables.hasNext()) {
				iterTables.next().writeXml(writer, false);
			}
		}

		writer.append(XmlUtilities.close(Schema.TAG_NAME));
	}

	/**
	 * This method processes returning the specified table.
	 * 
	 * @param request HttpServletRequest which is making the request.
	 * @param response HttpServletResponse where the results are to be returned.
	 * @throws IOException
	 */
	private void processTable(final HttpServletRequest request, final Table table, final HttpServletResponse response)
			throws IOException {
		final List<MimeType> listAccepts = getAccepts(request);

		for (final MimeType mimeType : listAccepts) {
			if (MIME_JSON.match(mimeType)) {
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType(MIME_JSON.toString());

				final PrintWriter out = response.getWriter();

				final JsonTable jsonTable = new JsonTable(table);

				jsonTable.get().writeJSONString(out);
				break;
			}
			else if (MIME_XML.match(mimeType)) {
				final PrintWriter writer = returnXml(response, HttpServletResponse.SC_OK);

				writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

				table.writeXml(writer);
				break;
			}
		}
	}

	/**
	 * This method returns the contents of the tables.
	 * 
	 * @param element
	 * @param response
	 * @throws IOException
	 * @throws SQLException
	 * @throws SqlExecutionException
	 * @throws SqlBuilderException
	 * @throws SqlManagerException
	 * @throws DatabaseException
	 */
	private void retrieveTable(final Table table, final HttpServletResponse response) throws SqlManagerException,
			SqlBuilderException, SqlExecutionException, SQLException, IOException, DatabaseException {
		final PrintWriter writer = returnXml(response, HttpServletResponse.SC_OK);

		final SqlQuery sqlQuery = new SqlQuery();

		sqlQuery.add(table, true);

		final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();

		writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

		writer.append(XmlUtilities.open(Parser.TAG_DATA));

		manager.getSqlManager().execute(sqlQuery, new QueryResults(writer));

		writer.append(XmlUtilities.close(Parser.TAG_DATA));
	}

	/**
	 * This class is used to process the results from a table query.
	 */
	private static class QueryResults extends SqlManager.SqlResultAction {
		private static final String COLUMNS = "columns";

		/** This member holds the writer used when dumping the results. */
		private final Writer m_writer;

		/**
		 * This method constructs a new query processor.
		 */
		public QueryResults(final Writer writer) {
			m_writer = writer;
		}

		/**
		 * This method is called after the last row is processed. This method is only called if there are rows to be
		 * processed.
		 * 
		 * @throws IOException
		 */
		@Override
		public void afterRows(final SqlExecution sqlExecution) throws IOException {
			m_writer.append(XmlUtilities.close(Parser.TAG_ITEMS));
		}

		/**
		 * This method is called prior to the first row being processed. This method is only called if there are rows to
		 * be processed.
		 * 
		 * @throws IOException
		 */
		@Override
		public void beforeRows(final SqlExecution sqlExecution) throws IOException {
			m_writer.append(XmlUtilities.open(COLUMNS));

			final Iterator<Column> iterColumnMap = sqlExecution.getColumnMap();
			if (iterColumnMap != null && iterColumnMap.hasNext()) {
				while (iterColumnMap.hasNext()) {
					final Column column = iterColumnMap.next();
					final List<NameValuePair> listAttributes = new ArrayList<NameValuePair>();

					listAttributes.add(new NameValuePair(Element.ATTRIBUTE_NAME, column.getName()));
					listAttributes.add(new NameValuePair("fullname", column.getFQName()));
					listAttributes.add(new NameValuePair(ColumnDefinition.ATTRIBUTE_TYPE, column.getType().getType()));

					m_writer.append(XmlUtilities.tag(ColumnDefinition.TAG_NAME, listAttributes));
				}
			}

			m_writer.append(XmlUtilities.close(COLUMNS));
			m_writer.append(XmlUtilities.open(Parser.TAG_ITEMS));
		}

		/**
		 * This method is called for each row returned in the result set.
		 * 
		 * @param sqlExecution SqlExecution instance used to execute the query.
		 * @param rs ResultSet being processed.
		 * @throws IOException
		 * @throws DatabaseException
		 * @throws SQLException
		 */
		@Override
		public void process(final SqlExecution sqlExecution, final ResultSet rs) throws IOException, SQLException,
				DatabaseException {
			m_writer.append(XmlUtilities.open(Parser.TAG_ITEM));

			try {
				final Iterator<Column> iterColumnMap = sqlExecution.getColumnMap();
				if (iterColumnMap != null && iterColumnMap.hasNext()) {
					while (iterColumnMap.hasNext()) {
						final Column column = iterColumnMap.next();
						final Object objValue = sqlExecution.getObject(column, rs);

						m_writer.append(XmlUtilities.tag(ColumnDefinition.TAG_NAME, new NameValuePair(
								Element.ATTRIBUTE_NAME, column.getName()), objValue));
					}
				}
			}
			finally {
				m_writer.append(XmlUtilities.close(Parser.TAG_ITEM));
			}
		}
	}
}

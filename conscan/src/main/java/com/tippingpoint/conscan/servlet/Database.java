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
import javax.activation.MimeType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
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
			final List<Element> listElements = ServiceBase.getElements(strObjects);
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
			final List<Element> listElements = ServiceBase.getElements(strObjects);
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
				final List<Element> listElements = ServiceBase.getElements(strObjects);
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

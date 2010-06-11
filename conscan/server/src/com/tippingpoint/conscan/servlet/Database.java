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
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.DatabaseElementException;
import com.tippingpoint.database.DatabaseException;
import com.tippingpoint.database.Element;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
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
			final List<Element> listElements = getObjects(strObjects);
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
	 * database/activity - return the contents of the activity table
	 * database/activity?column=value - return the contents of the activity table for the value in the specified column
	 * 
	 * @throws IOException
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final String strObjects = request.getPathInfo();

		m_log.debug("Get: " + strObjects);
		
		try {
			final List<Element> listElements = getObjects(strObjects);
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
		catch (SqlManagerException e) {
			m_log.error("SQL manager error retrieving table information.", e);
			processException(response, e);
		}
		catch (SqlBuilderException e) {
			m_log.error("SQL builder error retrieving table information.", e);
			processException(response, e);
		}
		catch (SqlExecutionException e) {
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
			final List<Element> listElements = getObjects(strObjects);
			switch (listElements.size()) {
			case 0:
				final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
				processSchema(manager.getSchema(), response);
			break;

			case 1:
				final Element element = listElements.get(0);
				if (element instanceof Table) {
					processTable((Table)element, response);
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
	}

	/**
	 * This method breaks down the string used to identify the object.
	 * 
	 * @param strObjects String containing the path information.
	 * @throws SQLException
	 * @throws DatabaseElementException
	 */
	private List<Element> getObjects(final String strObjects) throws DatabaseElementException, SQLException {
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
	 * This method returns and XML string representing the exception.
	 * 
	 * @throws IOException
	 */
	private void processException(final HttpServletResponse response, Throwable t) throws IOException {
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		response.setContentType("xml/application");

		final PrintWriter writer = response.getWriter();

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
	 * This method processes returning all the tables in the schema.
	 * 
	 * @param response HttpServletResponse where the results are to be returned.
	 * @throws IOException
	 */
	private void processSchema(final Schema schema, final HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("xml/application");

		final PrintWriter writer = response.getWriter();

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
	 * This method processes returning all the tables in the schema.
	 * 
	 * @param response HttpServletResponse where the results are to be returned.
	 * @throws IOException
	 */
	private void processTable(final Table table, final HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("xml/application");

		final PrintWriter writer = response.getWriter();

		table.writeXml(writer);
	}

	/**
	 * This method returns the contents of the tables.
	 * @param element
	 * @param response
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws SqlExecutionException 
	 * @throws SqlBuilderException 
	 * @throws SqlManagerException 
	 */
	private void retrieveTable(Table table, HttpServletResponse response) throws SqlManagerException, SqlBuilderException, SqlExecutionException, SQLException, IOException {
		SqlQuery sqlQuery = new SqlQuery();
		
		sqlQuery.add(table, true);

		ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
		
		manager.getSqlManager().execute(sqlQuery, new QueryResults(response.getWriter()));
	}
	
	/**
	 * This class is used to process the results from a table query. 
	 */
	private static class QueryResults extends SqlManager.SqlResultAction {
		/** This member holds the writer used when dumping the results. */
		private Writer m_writer;

		/**
		 * This method constructs a new query processor.
		 */
		public QueryResults(Writer writer) {
			m_writer = writer;
		}
		
		/**
		 * This method is called for each row returned in the result set.
		 * @param rs ResultSet being processed.
		 */
		@Override
		public void process(ResultSet rs) {
		}

		/**
		 * This method is called prior to the results set being generated.
		 * @param sqlExecution SqlExecution used to retrieve the result set.
		 * @throws IOException 
		 */
		@Override
		public void execution(SqlExecution sqlExecution) throws IOException {
			m_writer.append(XmlUtilities.open("columns"));
			
			Iterator<Entry<Column, Integer>> iterColumnMap = sqlExecution.getColumnMap();
			if (iterColumnMap != null && iterColumnMap.hasNext()) {
				while (iterColumnMap.hasNext()) {
					Map.Entry<Column, Integer> entry = (Map.Entry<Column, Integer>)iterColumnMap.next();
					
					List<NameValuePair> listAttributes = new ArrayList<NameValuePair>();
					
					listAttributes.add(new NameValuePair(Element.ATTRIBUTE_NAME, entry.getKey().getName()));
					listAttributes.add(new NameValuePair("fullname", entry.getKey().getFQName()));
					listAttributes.add(new NameValuePair(ColumnDefinition.ATTRIBUTE_TYPE, entry.getKey().getType().getType()));
					
					m_writer.append(XmlUtilities.tag("column", listAttributes));
				}
			}
			
			m_writer.append(XmlUtilities.close("columns"));
		}
	}
}

package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.DatabaseElementException;
import com.tippingpoint.database.DatabaseException;
import com.tippingpoint.database.Element;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;
import com.tippingpoint.sql.SqlAlter;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.sql.SqlDrop;

public final class Database extends HttpServlet {
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
					SqlAlter sqlAlter = new SqlAlter((Table)listElements.get(0));
					
					Element element = listElements.get(1);
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
						} else {
							Constraint constraint = table.getConstraint(listObjects.get(1));
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
}

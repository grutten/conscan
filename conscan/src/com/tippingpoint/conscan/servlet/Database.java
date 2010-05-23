package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.DatabaseElementException;
import com.tippingpoint.database.Element;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.sql.SqlDrop;

public final class Database extends HttpServlet {
	private static Log m_log = LogFactory.getLog(Database.class);
	private static final long serialVersionUID = 1389375741587926242L;

	/**
	 * This method executes the delete command.
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String strObjects = request.getPathInfo();
		
		m_log.debug("Delete: " + strObjects);
		
		try {
			List<Element> listElements = getObjects(strObjects);
			if (listElements != null && !listElements.isEmpty()) {
				switch (listElements.size()) {
				case 1:
					SqlDrop sqlDrop = new SqlDrop((Table)listElements.get(0));

					ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
					manager.getSqlManager().executeUpdate(sqlDrop);
				break;
	
				default:
					// FUTURE: possibly alter to drop columns
				break;
				}
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, strObjects);
			}
		}
		catch (DatabaseElementException e) {
			m_log.error("Database error deleting table.", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
		catch (SqlBaseException e) {
			m_log.error("Error deleting table.", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
		catch (SQLException e) {
			m_log.error("SQL error deleting table.", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * This method breaks down the string used to identify the object.
	 * @param strObjects String containing the path information.
	 * @throws SQLException 
	 * @throws DatabaseElementException 
	 */
	private List<Element> getObjects(String strObjects) throws DatabaseElementException, SQLException {
		List<Element> listElements = new ArrayList<Element>();

		// convert the path string of type 'table/column' to an array of strings
		if (StringUtils.isNotBlank(strObjects)) {
			List<String> listObjects = new ArrayList<String>();
			StringTokenizer tokenizer = new StringTokenizer(strObjects, "/");
			while (tokenizer.hasMoreTokens()) {
				String strObject = StringUtils.trimToNull(tokenizer.nextToken());
				if (strObject != null) {
					listObjects.add(strObject);
				}
			}
			
			// if strings were specified, then convert to elements
			if (listObjects.size() > 0) {
				ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
				Schema schema = manager.getSchema(manager.getConnectionSource().getSchema());
				
				Table table = schema.getTable(listObjects.get(0));
				if (table != null) {
					listElements.add(table);
					
					// if there are more objects, then find the column
					if (listElements.size() > 1) {
						ColumnDefinition column = table.getColumn(listObjects.get(1));
						if (column != null) {
							listElements.add(column);
						}
					}
				}
			}
		}

		return listElements;
	}
}

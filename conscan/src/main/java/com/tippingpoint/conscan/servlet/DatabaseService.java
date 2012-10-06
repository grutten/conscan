package com.tippingpoint.conscan.servlet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;

import com.sun.jersey.spi.resource.Singleton;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.DatabaseElementException;
import com.tippingpoint.database.Element;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.database.json.JsonTable;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;
import com.tippingpoint.sql.SqlExecutionException;

/**
 * This class is used to service the activity requests.
 */
@Singleton
@Path("/database")
public class DatabaseService extends ServiceBase {
	/**
	 * This method constructs a new service.
	 */
	public DatabaseService() {
		super("database");
	}
	
	@OPTIONS
	@Path("/{tableName:[a-z]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTable(@PathParam("tableName") final String strTableName) 
			throws DatabaseElementException, SqlExecutionException, SQLException {
		String strOutput = null;
		final List<Element> listElements = getElements(strTableName);
		final Element element = listElements.get(0);
		if (element instanceof Table) {
			final JsonTable jsonTable = new JsonTable((Table)element);
			strOutput = jsonTable.get().toJSONString();
		}

		return strOutput;
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

}

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
	

}

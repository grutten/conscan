package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.DatabaseException;
import com.tippingpoint.database.ForeignKey;
import com.tippingpoint.database.Id;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.sql.SqlBuilderException;
import com.tippingpoint.sql.SqlExecutionException;
import com.tippingpoint.sql.SqlManagerException;
import com.tippingpoint.sql.SqlQuery;
import com.tippingpoint.sql.base.SqlExecution;
import com.tippingpoint.sql.base.SqlManager;
import com.tippingpoint.utilities.NameValuePair;
import com.tippingpoint.utilities.XmlUtilities;

public final class Scanner extends Services {
	private static Log m_log = LogFactory.getLog(Scanner.class);
	private static final long serialVersionUID = -1452761419695976821L;

	/**
	 * This method executes the options command; which is used to return the activities available to the current user.
	 * 
	 * @throws IOException
	 */
	@Override
	protected void doOptions(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final String strObjects = request.getPathInfo();

		m_log.debug(request.getMethod() + ": " + strObjects);

		final PrintWriter writer = returnXml(response, HttpServletResponse.SC_OK);

		try {
			getConfiguration(writer);
		}
		catch (final SqlBaseException e) {
			processException(response, e);
		}
		catch (final DatabaseException e) {
			processException(response, e);
		}
	}

	/**
	 * This method returns the execution to return the activities available.
	 * 
	 * @throws SqlManagerException
	 * @throws SqlBuilderException
	 */
	private SqlExecution getActivityExecution() throws SqlManagerException, SqlBuilderException {
		final Schema schema = getSchema();
		final SqlQuery sqlQuery = new SqlQuery();

		sqlQuery.add(schema.getTable("activity"), true);

		return getSqlManager().getExecution(sqlQuery);
	}

	/**
	 * This method retrieves the current configuration.
	 * 
	 * @param writer PrintWriter where the details are returned.
	 * @throws IOException
	 * @throws SqlBaseException
	 * @throws DatabaseException
	 */
	private void getConfiguration(final Writer writer) throws IOException, SqlBaseException, DatabaseException {

		writer.write("<configuration>");
		writeLocations(writer);
		writer.write("	<complianceconfigurations>");
		writeComplianceConfigurations(writer);
		writer.write("	</complianceconfigurations>");
		writer.write("	<activities>");
		writeActivities(writer);
		writer.write("	</activities>");
		writer.write("</configuration>");
	}

	/**
	 * This method returns the execution to return the compliance values available.
	 * 
	 * @throws SqlManagerException
	 * @throws SqlBuilderException
	 */
	private SqlExecution getConfigurationExecution() throws SqlManagerException, SqlBuilderException {
		final Schema schema = getSchema();
		final SqlQuery sqlQuery = new SqlQuery();

		sqlQuery.setAssociativeJoins(true);

		sqlQuery.add(schema.getTable("compliance"), true);
		sqlQuery.add(schema.getTable("compliancevalue"), true);

		return getSqlManager().getExecution(sqlQuery);
	}

	/**
	 * This method returns the connection manager in use.
	 */
	private ConnectionManager getManager() {
		final ConnectionManagerFactory factory = ConnectionManagerFactory.getFactory();
		return factory.getDefaultManager();
	}

	/**
	 * This method returns the schema in use.
	 */
	private Schema getSchema() {
		return getManager().getSchema();
	}

	/**
	 * This method returns the SQL manager in use.
	 */
	private SqlManager getSqlManager() {
		return getManager().getSqlManager();
	}

	/**
	 * This method writes the activities to the writer.
	 * 
	 * @param writer Writer used for writing out the XML.
	 * @throws SqlManagerException
	 * @throws SqlBuilderException
	 * @throws SqlExecutionException
	 * @throws DatabaseException
	 * @throws IOException
	 */
	private void writeActivities(final Writer writer) throws SqlManagerException, SqlBuilderException,
			SqlExecutionException, DatabaseException, IOException {
		final SqlExecution sqlExecution = getActivityExecution();

		Connection conn = null;
		ResultSet rs = null;

		try {
			final Table tableActivity = getSchema().getTable("activity");
			final Column columnActivityId = tableActivity.getPrimaryKeyColumn();

			conn = getManager().getConnection();
			rs = sqlExecution.executeQuery(conn);
			while (rs.next()) {
				final Id idActivity = (Id)sqlExecution.getObject(columnActivityId, rs);
				if (idActivity != null) {
					final NameValuePair pair = new NameValuePair(columnActivityId.getName(), idActivity.toString());

					writer.write(XmlUtilities.open("activity", pair));

					final Iterator<ColumnDefinition> iterColumns = tableActivity.getColumns();
					if (iterColumns != null && iterColumns.hasNext()) {
						while (iterColumns.hasNext()) {
							final Column column = iterColumns.next();
							if (!column.equals(columnActivityId)) {
								writer.write(XmlUtilities.tag(column.getName(), null, sqlExecution
										.getObject(column, rs)));
							}
						}
					}

					writer.write(XmlUtilities.close("activity"));
				}
			}
		}
		catch (final SQLException e) {
			throw new SqlExecutionException("SQL exception retrieving compliance configurations.", e);
		}
		finally {
			DbUtils.closeQuietly(conn, null, rs);
		}

		// writer.write("		<!-- SCANTYPE: 1 - cell, 2 - offender -->");
		// writer.write("		<!-- COMPLIANCETYPE: 1 - cell, 2 - offender -->");
		// writer.write("		<activity>");
		// writer.write("			<name>security check</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>1</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>cell search</name>");
		// writer.write("			<complianceconfiguration id=\"3\"/>");
		// writer.write("			<scantype>1</scantype>");
		// writer.write("			<compliancetype>1</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>bedding exchange</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>blanket exchange</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>outerwear exchange</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>underwear exchange</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>towel exchange</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>showers</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>recreation</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>library</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>mail call</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>meals</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>dayroom/indoor recreation</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>religious services</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>school</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>stores/canteen</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>video orient</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>telephones</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>legal mail</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>sick call</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>medical/dental</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>psych</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>visits</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>attorney visit</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>out to court</name>");
		// writer.write("			<complianceconfiguration id=\"1\"/>");
		// writer.write("			<scantype>2</scantype>");
		// writer.write("			<compliancetype>2</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>pill call</name>");
		// writer.write("			<complianceconfiguration id=\"2\"/>");
		// writer.write("			<scantype>1</scantype>");
		// writer.write("			<compliancetype>1</compliancetype>");
		// writer.write("		</activity>");
		// writer.write("		<activity>");
		// writer.write("			<name>repair order</name>");
		// writer.write("			<complianceconfiguration id=\"2\"/>");
		// writer.write("			<scantype>1</scantype>");
		// writer.write("			<compliancetype>1</compliancetype>");
		// writer.write("		</activity>");
	}

	/**
	 * This method writes the compliance configurations to the writer.
	 * 
	 * @param writer Writer used for writing out the XML.
	 * @throws SqlManagerException
	 * @throws SqlBuilderException
	 * @throws SqlExecutionException
	 * @throws DatabaseException
	 * @throws IOException
	 */
	private void writeComplianceConfigurations(final Writer writer) throws SqlManagerException, SqlBuilderException,
			SqlExecutionException, DatabaseException, IOException {
		final SqlExecution sqlExecution = getConfigurationExecution();

		Connection conn = null;
		ResultSet rs = null;

		try {
			Id idLastCompliance = null;
			boolean bComplianceValue = false;

			final Table tableCompliance = getSchema().getTable("compliance");
			final Column columnComplianceId = tableCompliance.getPrimaryKeyColumn();
			final Table tableComplianceValue = getSchema().getTable("compliancevalue");
			final Column columnComplianceValueId = tableComplianceValue.getPrimaryKeyColumn();
			Column columnComplianceValueChildId = null;

			final ForeignKey foreignKey = tableComplianceValue.getForeignKey(columnComplianceId);
			if (foreignKey != null) {
				columnComplianceValueChildId = foreignKey.getChildColumn();
			}

			conn = getManager().getConnection();
			rs = sqlExecution.executeQuery(conn);
			while (rs.next()) {
				final Id idCompliance = (Id)sqlExecution.getObject(columnComplianceId, rs);
				if (idLastCompliance == null || !idLastCompliance.equals(idCompliance)) {
					if (idLastCompliance != null) {
						if (bComplianceValue) {
							// close out the previously opened compliance value
							writer.write(XmlUtilities.close("compliancevalues"));

							bComplianceValue = false;
						}

						// close out the previously opened compliance configuration
						writer.write(XmlUtilities.close("complianceconfiguration"));
					}

					final NameValuePair pair = new NameValuePair(columnComplianceId.getName(), idCompliance.toString());

					writer.write(XmlUtilities.open("complianceconfiguration", pair));

					final Iterator<ColumnDefinition> iterColumns = tableCompliance.getColumns();
					if (iterColumns != null && iterColumns.hasNext()) {
						while (iterColumns.hasNext()) {
							final Column column = iterColumns.next();
							if (!column.equals(columnComplianceId)) {
								writer.write(XmlUtilities.tag(column.getName(), null, sqlExecution
										.getObject(column, rs)));
							}
						}
					}

					idLastCompliance = idCompliance;
				}

				final Id idComplianceValue = (Id)sqlExecution.getObject(columnComplianceValueId, rs);
				if (idComplianceValue != null) {
					if (!bComplianceValue) {
						writer.write(XmlUtilities.open("compliancevalues"));
						bComplianceValue = true;
					}

					final NameValuePair pair =
						new NameValuePair(columnComplianceValueId.getName(), idComplianceValue.toString());

					writer.write(XmlUtilities.open("compliancevalue", pair));

					final Iterator<ColumnDefinition> iterColumns = tableComplianceValue.getColumns();
					if (iterColumns != null && iterColumns.hasNext()) {
						while (iterColumns.hasNext()) {
							final Column column = iterColumns.next();
							if (!column.equals(columnComplianceValueId) && !column.equals(columnComplianceValueChildId)) {
								writer.write(XmlUtilities.tag(column.getName(), null, sqlExecution
										.getObject(column, rs)));
							}
						}
					}

					writer.write(XmlUtilities.close("compliancevalue"));
				}
			}

			if (idLastCompliance != null) {
				if (bComplianceValue) {
					// close out the previously opened compliance value
					writer.write(XmlUtilities.close("compliancevalues"));
				}

				// close out the previously opened compliance configuration
				writer.write(XmlUtilities.close("complianceconfiguration"));
			}
		}
		catch (final SQLException e) {
			throw new SqlExecutionException("SQL exception retrieving compliance configurations.", e);
		}
		finally {
			DbUtils.closeQuietly(conn, null, rs);
		}

		// writer.write("		<complianceconfiguration id=\"1\">");
		// writer.write("			<name>default</name>");
		// writer.write("			<compliancevalues>");
		// writer.write("				<value default=\"true\">comply</value>");
		// writer.write("				<value>refuse</value>");
		// writer.write("			</compliancevalues>");
		// writer.write("		</complianceconfiguration>");
		// writer.write("		<complianceconfiguration id=\"2\">");
		// writer.write("			<name>workorder</name>");
		// writer.write("			<compliancevalues>");
		// writer.write("				<value default=\"true\">comply</value>");
		// writer.write("				<value>cleanliness</value>");
		// writer.write("				<value>paint</value>");
		// writer.write("				<value>lights</value>");
		// writer.write("				<value>sinks/toilets/showers</value>");
		// writer.write("				<value>clean/office supplies</value>");
		// writer.write("				<value>hot water</value>");
		// writer.write("				<value>heat/air cond</value>");
		// writer.write("				<value>doors/locks/gates</value>");
		// writer.write("				<value>phones/tv/vending</value>");
		// writer.write("			</compliancevalues>");
		// writer.write("		</complianceconfiguration>");
		// writer.write("		<complianceconfiguration id=\"3\">");
		// writer.write("			<name>cellsearch</name>");
		// writer.write("			<compliancevalues>");
		// writer.write("				<value default=\"true\">nothing found</value>");
		// writer.write("				<value>cell phone</value>");
		// writer.write("				<value>drugs</value>");
		// writer.write("				<value>excessive property</value>");
		// writer.write("				<value>i/m alcohol</value>");
		// writer.write("				<value>smoking materials</value>");
		// writer.write("				<value>weapon</value>");
		// writer.write("				<value>other</value>");
		// writer.write("			</compliancevalues>");
		// writer.write("		</complianceconfiguration>");
		// writer.write("		<complianceconfiguration id=\"4\">");
		// writer.write("			<name>pillcallx</name>");
		// writer.write("			<compliancevalues>");
		// writer.write("				<value default=\"true\">administered</value>");
		// writer.write("			</compliancevalues>");
		// writer.write("		</complianceconfiguration>");
	}

	/**
	 * This method writes the locations to the writer.
	 * 
	 * @param writer Writer used for writing out the XML.
	 * @throws SqlBaseException
	 * @throws IOException
	 */
	private void writeLocations(final Writer writer) throws SqlBaseException, IOException {
		final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder("location");
		if (builder != null) {
			final List<BusinessObject> listLocations = builder.getAll();
			if (listLocations != null && !listLocations.isEmpty()) {
				writer.write(XmlUtilities.open("locations"));

				for (final BusinessObject businessObject : listLocations) {
					writeObject(writer, businessObject);
				}

				writer.write(XmlUtilities.close("locations"));
			}
		}
	}
}

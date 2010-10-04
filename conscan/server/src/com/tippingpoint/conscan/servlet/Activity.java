package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class Activity extends BaseTableService {
	private static Log m_log = LogFactory.getLog(Activity.class);
	private static final long serialVersionUID = -6955903582399234823L;

	public Activity() {
		super("activity");
	}

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

		getConfiguration(writer);
	}

	/**
	 * This method retrieves the current configuration.
	 * 
	 * @param writer PrintWriter where the details are returned.
	 * @throws IOException
	 */
	private void getConfiguration(final Writer writer) throws IOException {
		writer.write("<configuration>");
		writer.write("	<tier>");
		writer.write("		<name>tier1</name>");
		writer.write("		<locations>");
		writer.write("			<location>");
		writer.write("				<name>T162-A-012L</name>");
		writer.write("				<barcode>0070718001170</barcode>");
		writer.write("				<offenders>");
		writer.write("					<offender>");
		writer.write("						<name>Iverson</name>");
		writer.write("						<bookingnumber>345345345</bookingnumber>");
		writer.write("						<barcode>0812122010450</barcode>");
		writer.write("					</offender>");
		writer.write("					<offender>");
		writer.write("						<name>Page</name>");
		writer.write("						<bookingnumber>123456789</bookingnumber>");
		writer.write("						<barcode>0812122010160</barcode>");
		writer.write("					</offender>");
		writer.write("				</offenders>");
		writer.write("			</location>");
		writer.write("			<location>");
		writer.write("				<name>T162-A-011L</name>");
		writer.write("				<barcode>L987654111</barcode>");
		writer.write("				<offenders>");
		writer.write("					<offender>");
		writer.write("						<name>Irving</name>");
		writer.write("						<bookingnumber>234567890</bookingnumber>");
		writer.write("						<barcode>0832924005201</barcode>");
		writer.write("					</offender>");
		writer.write("				</offenders>");
		writer.write("			</location>");
		writer.write("		</locations>");
		writer.write("	</tier>");
		writer.write("	<complianceconfigurations>");
		writer.write("		<complianceconfiguration id=\"1\">");
		writer.write("			<name>default</name>");
		writer.write("			<compliancevalues>");
		writer.write("				<value default=\"true\">comply</value>");
		writer.write("				<value>refuse</value>");
		writer.write("			</compliancevalues>");
		writer.write("		</complianceconfiguration>");
		writer.write("		<complianceconfiguration id=\"2\">");
		writer.write("			<name>workorder</name>");
		writer.write("			<compliancevalues>");
		writer.write("				<value default=\"true\">comply</value>");
		writer.write("				<value>cleanliness</value>");
		writer.write("				<value>paint</value>");
		writer.write("				<value>lights</value>");
		writer.write("				<value>sinks/toilets/showers</value>");
		writer.write("				<value>clean/office supplies</value>");
		writer.write("				<value>hot water</value>");
		writer.write("				<value>heat/air cond</value>");
		writer.write("				<value>doors/locks/gates</value>");
		writer.write("				<value>phones/tv/vending</value>");
		writer.write("			</compliancevalues>");
		writer.write("		</complianceconfiguration>");
		writer.write("		<complianceconfiguration id=\"3\">");
		writer.write("			<name>cellsearch</name>");
		writer.write("			<compliancevalues>");
		writer.write("				<value default=\"true\">nothing found</value>");
		writer.write("				<value>cell phone</value>");
		writer.write("				<value>drugs</value>");
		writer.write("				<value>excessive property</value>");
		writer.write("				<value>i/m alcohol</value>");
		writer.write("				<value>smoking materials</value>");
		writer.write("				<value>weapon</value>");
		writer.write("				<value>other</value>");
		writer.write("			</compliancevalues>");
		writer.write("		</complianceconfiguration>");
		writer.write("		<complianceconfiguration id=\"4\">");
		writer.write("			<name>pillcallx</name>");
		writer.write("			<compliancevalues>");
		writer.write("				<value default=\"true\">administered</value>");
		writer.write("			</compliancevalues>");
		writer.write("		</complianceconfiguration>");
		writer.write("	</complianceconfigurations>");
		writer.write("	<activities>");
		writer.write("		<!-- SCANTYPE: 1 - cell, 2 - offender -->");
		writer.write("		<!-- COMPLIANCETYPE: 1 - cell, 2 - offender -->");
		writer.write("		<activity>");
		writer.write("			<name>security check</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>1</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>cell search</name>");
		writer.write("			<complianceconfiguration id=\"3\"/>");
		writer.write("			<scantype>1</scantype>");
		writer.write("			<compliancetype>1</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>bedding exchange</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>blanket exchange</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>outerwear exchange</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>underwear exchange</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>towel exchange</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>showers</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>recreation</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>library</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>mail call</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>meals</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>dayroom/indoor recreation</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>religious services</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>school</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>stores/canteen</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>video orient</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>telephones</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>legal mail</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>sick call</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>medical/dental</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>psych</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>visits</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>attorney visit</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>out to court</name>");
		writer.write("			<complianceconfiguration id=\"1\"/>");
		writer.write("			<scantype>2</scantype>");
		writer.write("			<compliancetype>2</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>pill call</name>");
		writer.write("			<complianceconfiguration id=\"2\"/>");
		writer.write("			<scantype>1</scantype>");
		writer.write("			<compliancetype>1</compliancetype>");
		writer.write("		</activity>");
		writer.write("		<activity>");
		writer.write("			<name>repair order</name>");
		writer.write("			<complianceconfiguration id=\"2\"/>");
		writer.write("			<scantype>1</scantype>");
		writer.write("			<compliancetype>1</compliancetype>");
		writer.write("		</activity>");
		writer.write("	</activities>");
		writer.write("</configuration>");
	}
}

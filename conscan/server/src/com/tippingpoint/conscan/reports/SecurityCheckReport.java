package com.tippingpoint.conscan.reports;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.sun.jersey.spi.resource.Singleton;

/**
 * This class generates the security check report.
 */
@Singleton
@Path("/report/security")
public class SecurityCheckReport extends ReportBase {
	private static final String PARAM_END_DATE = "EndDate";
	private static final String PARAM_PRINTED_BY = "PrintedBy";
	private static final String PARAM_PRINTED_DATE = "PrintedDate";
	private static final String PARAM_START_DATE = "StartDate";
	private static final String PARAM_TITLE = "Title";

	/**
	 * This method constructs a new named report.
	 */
	public SecurityCheckReport() {
		super("securitycheck.jrxml");
	}

	@SuppressWarnings("unchecked")
	@OPTIONS
	@Produces(MediaType.APPLICATION_JSON)
	public String reportoptions() {
		final JSONObject definition = new JSONObject();

		definition.put("name", "security");

		final JSONArray options = new JSONArray();
		definition.put("options", options);

		JSONObject option = new JSONObject();

		option.put("name", PARAM_START_DATE);
		option.put("type", "timestamp");

		options.add(option);

		option = new JSONObject();

		option.put("name", PARAM_END_DATE);
		option.put("type", "timestamp");

		options.add(option);

		return definition.toJSONString();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public StreamingOutput security(@QueryParam(PARAM_START_DATE) final String strStartDate,
			@QueryParam(PARAM_END_DATE) final String strEndDate) {
		final Map<String, Object> mapParameters = new HashMap<String, Object>();

		mapParameters.put(PARAM_START_DATE, getDate(strStartDate));
		mapParameters.put(PARAM_END_DATE, getDate(strEndDate));
		mapParameters.put(PARAM_TITLE, "Local County Sherriff's Department");
		mapParameters.put(PARAM_PRINTED_BY, "William Rosewood");
		mapParameters.put(PARAM_PRINTED_DATE, new Date());

		return generateReportStreamHtml(mapParameters);
	}

	@GET
	@Produces("application/pdf")
	@Path("{report}.pdf")
	public StreamingOutput securityPdf(@QueryParam(PARAM_START_DATE) final String strStartDate,
			@QueryParam(PARAM_END_DATE) final String strEndDate) {
		final Map<String, Object> mapParameters = new HashMap<String, Object>();

		mapParameters.put(PARAM_START_DATE, getDate(strStartDate));
		mapParameters.put(PARAM_END_DATE, getDate(strEndDate));
		mapParameters.put(PARAM_TITLE, "Local County Sherriff's Department");
		mapParameters.put(PARAM_PRINTED_BY, "William Rosewood");
		mapParameters.put(PARAM_PRINTED_DATE, new Date());

		return generateReportStreamPdf(mapParameters);
	}

	private Date getDate(final String strValue) {
		Date dtValue = null;

		if (strValue != null && strValue.length() > 0) {
			// sample date: 09/01/2011 00:00
			final DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
			try {
				dtValue = df.parse(strValue);
			}
			catch (final ParseException e) {
				dtValue = new Date(); // default to now
			}
		}

		return dtValue;
	}
}

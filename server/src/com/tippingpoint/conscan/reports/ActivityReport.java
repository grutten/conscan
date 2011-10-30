package com.tippingpoint.conscan.reports;

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
@Path("/report/activity")
public class ActivityReport extends ReportBase {
	private static final String PARAM_END_DATE = "EndDate";
	private static final String PARAM_PRINTED_BY = "PrintedBy";
	private static final String PARAM_PRINTED_DATE = "PrintedDate";
	private static final String PARAM_START_DATE = "StartDate";
	private static final String PARAM_TITLE = "Title";

	/**
	 * This method constructs a new named report.
	 */
	public ActivityReport() {
		super("activity.jrxml");
	}

	@SuppressWarnings("unchecked")
	@OPTIONS
	@Produces(MediaType.APPLICATION_JSON)
	public String reportoptions() {
		final JSONObject definition = new JSONObject();

		definition.put("name", "activity");

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
	public StreamingOutput report(@QueryParam(PARAM_START_DATE) final String strStartDate,
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
	public StreamingOutput reportPdf(@QueryParam(PARAM_START_DATE) final String strStartDate,
			@QueryParam(PARAM_END_DATE) final String strEndDate) {
		final Map<String, Object> mapParameters = new HashMap<String, Object>();

		mapParameters.put(PARAM_START_DATE, getDate(strStartDate));
		mapParameters.put(PARAM_END_DATE, getDate(strEndDate));
		mapParameters.put(PARAM_TITLE, "Local County Sherriff's Department");
		mapParameters.put(PARAM_PRINTED_BY, "William Rosewood");
		mapParameters.put(PARAM_PRINTED_DATE, new Date());

		return generateReportStreamPdf(mapParameters);
	}
}

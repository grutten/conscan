package com.tippingpoint.conscan.reports;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
	@SuppressWarnings("unchecked")
	@OPTIONS
	@Produces(MediaType.APPLICATION_JSON)
	public String reportoptions() {
		JSONArray options = new JSONArray();
		
		JSONObject option = new JSONObject();

		option.put("type", "timestamp");
		option.put("name", "start");
		
		options.add(option);
		
		option = new JSONObject();

		option.put("type", "timestamp");
		option.put("name", "end");
		
		options.add(option);

		return options.toJSONString();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public StreamingOutput security() {
		return new ReportOutput("securitycheck.jasper", null);
	}
}

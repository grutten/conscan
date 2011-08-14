package com.tippingpoint.conscan.reports;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import com.sun.jersey.spi.resource.Singleton;

/**
 * This class generates the security check report.
 */
@Singleton
@Path("/report")
public class SecurityCheckReport extends ReportBase {
	@GET
	@Path("/security")
	@Produces(MediaType.TEXT_HTML)
	public StreamingOutput security() {
		return new ReportOutput("securitycheck.jasper", null);
	}
}

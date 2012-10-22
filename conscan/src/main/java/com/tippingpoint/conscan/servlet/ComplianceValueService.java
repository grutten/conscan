package com.tippingpoint.conscan.servlet;

import javax.ws.rs.Path;

import com.sun.jersey.spi.resource.Singleton;

/**
 * This class is used to service the compliancevalue requests.
 */
@Singleton
@Path("/compliancevalue")
public class ComplianceValueService extends ServiceBase {
	/**
	 * This method constructs a new service.
	 */
	public ComplianceValueService() {
		super("compliancevalue");
	}

}

package com.tippingpoint.conscan.servlet;

import javax.ws.rs.Path;
import com.sun.jersey.spi.resource.Singleton;

/**
 * This class is used to service the compliance requests.
 */
@Singleton
@Path("/compliance")
public class ComplianceService extends ServiceBase {
	/**
	 * This method constructs a new service.
	 */
	public ComplianceService() {
		super("compliance");
	}
}

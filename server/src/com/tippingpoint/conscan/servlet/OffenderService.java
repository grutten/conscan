package com.tippingpoint.conscan.servlet;

import javax.ws.rs.Path;
import com.sun.jersey.spi.resource.Singleton;

/**
 * This class is used to service the offender requests.
 */
@Singleton
@Path("/offender")
public class OffenderService extends ServiceBase {
	/**
	 * This method constructs a new service.
	 */
	public OffenderService() {
		super("offender");
	}
}

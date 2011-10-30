package com.tippingpoint.conscan.servlet;

import javax.ws.rs.Path;
import com.sun.jersey.spi.resource.Singleton;

/**
 * This class is used to service the staff requests.
 */
@Singleton
@Path("/staff")
public class StaffService extends ServiceBase {
	/**
	 * This method constructs a new service.
	 */
	public StaffService() {
		super("staff");
	}
}

package com.tippingpoint.conscan.servlet;

import javax.ws.rs.Path;
import com.sun.jersey.spi.resource.Singleton;

/**
 * This class is used to service the activity requests.
 */
@Singleton
@Path("/activity")
public class ActivityService extends Service {
	/**
	 * This method constructs a new service.
	 */
	public ActivityService() {
		super("activity");
	}
}

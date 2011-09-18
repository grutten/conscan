package com.tippingpoint.conscan.servlet;

import javax.ws.rs.Path;
import com.sun.jersey.spi.resource.Singleton;

/**
 * This class is used to service the location requests.
 */
@Singleton
@Path("/location")
public class LocationService extends ServiceBase {
	/**
	 * This method constructs a new service.
	 */
	public LocationService() {
		super("location");
	}
}

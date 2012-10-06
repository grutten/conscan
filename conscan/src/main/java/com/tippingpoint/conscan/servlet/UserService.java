package com.tippingpoint.conscan.servlet;

import javax.ws.rs.Path;

import com.sun.jersey.spi.resource.Singleton;

/**
 * This class is used to service the activity requests.
 */
@Singleton
@Path("/user")
public class UserService extends ServiceBase {
	/**
	 * This method constructs a new service.
	 */
	public UserService() {
		super("user");
	}
}

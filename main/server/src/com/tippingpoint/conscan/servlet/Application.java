package com.tippingpoint.conscan.servlet;

import javax.ws.rs.ApplicationPath;
import com.sun.jersey.api.core.PackagesResourceConfig;

/**
 * This class is the base class for the resources.
 */
@ApplicationPath("/rest")
public class Application extends PackagesResourceConfig {
	public Application() {
		super("com.tippingpoint.conscan.servlet");
	}
}

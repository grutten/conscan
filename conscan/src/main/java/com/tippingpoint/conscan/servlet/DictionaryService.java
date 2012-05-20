package com.tippingpoint.conscan.servlet;

import javax.ws.rs.Path;
import com.sun.jersey.spi.resource.Singleton;

/**
 * This class is used to service the dictionary requests.
 */
@Singleton
@Path("/dictionary")
public class DictionaryService extends ServiceBase {
	public DictionaryService() {
		super("dictionary");
	}
}

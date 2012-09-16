package com.tippingpoint.conscan.servlet;

import javax.ws.rs.Path;
import com.sun.jersey.spi.resource.Singleton;

/**
 * This class is used to process log requests.
 */
@Singleton
@Path("/scanlog")
public class ScanLogService extends ServiceBase {
	public ScanLogService() {
		super("scanlog");
	}
}

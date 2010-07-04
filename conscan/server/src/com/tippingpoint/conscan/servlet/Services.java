package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is the base class for service based servlets.
 */
public abstract class Services extends HttpServlet {
	private static final long serialVersionUID = -5482024580102875533L;

	/**
	 * This method prepares the response for returning XML.
	 * 
	 * @param response HttpServletResponse that is being prepared.
	 * @param nStatus int containing the default status.
	 * @throws IOException
	 */
	protected PrintWriter returnXml(final HttpServletResponse response, final int nStatus) throws IOException {
		response.setStatus(nStatus);
		response.setContentType("text/xml");

		return response.getWriter();
	}
}

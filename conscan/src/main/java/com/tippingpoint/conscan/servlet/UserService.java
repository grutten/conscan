package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jersey.spi.resource.Singleton;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.conscan.objects.FieldValue;
import com.tippingpoint.sql.SqlBaseException;

/**
 * This class is used to service the activity requests.
 */
@Singleton
@Path("/user")
public class UserService extends ServiceBase {
	private static final String COOKIE_NAME = "user";
	private static Log m_log = LogFactory.getLog(User.class);

	/**
	 * This method constructs a new service.
	 */
	public UserService() {
		super("user");
	}
	
	/**
	 * This method returns the currently logged in user.
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public StreamingOutput getUser(@CookieParam(COOKIE_NAME) String strEmail) {
		StreamingOutput output = null;

		try {
			final BusinessObject boUser = getUser(strEmail, null);
			if (boUser != null)
				output = new ServiceOutputObjectXml(boUser, false);
		}
		catch (final SqlBaseException e) {
			m_log.error("Database error reading the current user.", e);
//			processException(response, e);
		}
		
		return output;
	}

	/**
	 * This method clears the session object.
	 */
	@DELETE
	public void logout(@Context HttpServletRequest request) {
		m_log.debug("Delete");

		// remove authentication object from session
		request.getSession().setAttribute(AuthenticationFilter.AUTH_OBJECT_NAME, null);
	}

	public static void setCookieExpiration(final HttpServletRequest request, final HttpServletResponse response, int nExpirationInSeconds) {
		// find the cookie to get the logged in user
		final Cookie cookie = getCookie(request);
		if (cookie != null) {
			// kill the cookie
			cookie.setMaxAge(0);

			response.addCookie(cookie);
		}
	}
	
	/**
	 * This method executes the put command; which is used to log a user into the system.
	 * 
	 * @throws IOException
	 */
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public StreamingOutput login(@Context HttpServletRequest request, 
			@QueryParam("user") final String strUser, 
			@QueryParam("password") final String strPassword) { 
		StreamingOutput output = null;

		if (strUser != null) {
			try {
				final BusinessObject boUser = getUser(strUser, strPassword);
				if (boUser != null) {
					output = new ServiceOutputObjectXml(boUser, false);

//					writeObject(out, boUser, false);
					final FieldValue fieldEmail = boUser.getValue("email");
					if (fieldEmail != null) {
						// Add session object
						HttpSession session = request.getSession();
						session.setAttribute(AuthenticationFilter.AUTH_OBJECT_NAME, Long.valueOf(session.getLastAccessedTime()));
/*
			    		StringProperties sp = SystemProperties.getSystemProperties().getStringProperties();
			    		String strTimeout = sp.getValue("authentication.timeout.minutes");
			    		int intAuthTimeout = Integer.valueOf(strTimeout == null ? "5" : strTimeout).intValue() * 60;
						
						final Cookie cookie = new Cookie(COOKIE_NAME, fieldEmail.getValue().toString());
						cookie.setMaxAge(intAuthTimeout); // set it as a session cookie
						response.addCookie(cookie);
*/						
					}
				}
			}
			catch (final SqlBaseException e) {
//				processException(response, e);
			}
		}
		
		return output;
	}

	/**
	 * This method returns the user cookie from the request.
	 */
	private static Cookie getCookie(final HttpServletRequest request) {
		Cookie foundCookie = null;

		final Cookie[] aCookies = request.getCookies();
		if (aCookies != null && aCookies.length > 0) {
			for (final Cookie cookie : aCookies) {
				if (COOKIE_NAME.equals(cookie.getName())) {
					foundCookie = cookie;
					break;
				}
			}
		}

		return foundCookie;
	}
	
	/**
	 * This method returns the user for the given parameters.
	 * 
	 * @throws SqlBaseException
	 */
	private BusinessObject getUser(final String strEmail, final String strPassword) throws SqlBaseException {
		BusinessObject boUser = null;
		if (strEmail != null) {
			final List<FieldValue> listParameters = new ArrayList<FieldValue>();

			listParameters.add(new FieldValue("email", strEmail));
			if (strPassword != null) {
				listParameters.add(new FieldValue("password", strPassword));
			}

			final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder("staff");
			final List<BusinessObject> listBusinessObject = builder.getAll(listParameters);

			if (listBusinessObject != null && listBusinessObject.size() == 1) {
				boUser = listBusinessObject.get(0);
			}
		}

		return boUser;
	}
}

package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.conscan.objects.FieldValue;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.utilities.StringProperties;
import com.tippingpoint.utilities.SystemProperties;

/**
 * This class is serves information pertaining to the current user.
 */
public class User extends Services {
	private static final String COOKIE_NAME = "user";
	private static Log m_log = LogFactory.getLog(User.class);
	private static final long serialVersionUID = 2641405874048058605L;

	/**
	 * This method executes the put command; which is used to log a user into the system.
	 * 
	 * @throws IOException
	 */
	@Override
	protected void doPut(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final Map<String, String> mapParameters = getParameterMap(request);

		final String strUser = mapParameters.get("user");
		final String strPassword = mapParameters.get("password");

		if (strUser != null) {
			try {
				final PrintWriter out = returnXml(response, HttpServletResponse.SC_OK);
				final BusinessObject boUser = getUser(strUser, strPassword);
				if (boUser != null) {
					writeObject(out, boUser, false);
					final FieldValue fieldEmail = boUser.getValue("email");
					if (fieldEmail != null) {
						// Add session object
						HttpSession session = request.getSession();
						session.setAttribute(AuthenticationFilter.AUTH_OBJECT_NAME, Long.valueOf(session.getLastAccessedTime()));

			    		StringProperties sp = SystemProperties.getSystemProperties().getStringProperties();
			    		String strTimeout = sp.getValue("authentication.timeout.minutes");
			    		int intAuthTimeout = Integer.valueOf(strTimeout == null ? "5" : strTimeout).intValue() * 60;
						
						final Cookie cookie = new Cookie(COOKIE_NAME, fieldEmail.getValue().toString());
						cookie.setMaxAge(intAuthTimeout); // set it as a session cookie
						response.addCookie(cookie);
					}
				}
				else {
					returnXml(response, HttpServletResponse.SC_NO_CONTENT);
				}
			}
			catch (final SqlBaseException e) {
				processException(response, e);
			}
		}
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

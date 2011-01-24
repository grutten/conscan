package com.tippingpoint.conscan.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.conscan.objects.FieldValue;
import com.tippingpoint.sql.SqlBaseException;

/**
 * This class is serves information pertaining to the current user.
 */
public class UserService extends Services {
	private static final String COOKIE_NAME = "user";
	private static Log m_log = LogFactory.getLog(UserService.class);
	private static final long serialVersionUID = 2641405874048058605L;

	/**
	 * This method executes the get command; which is used to return the current information used to identify the
	 * currently logged in user.
	 * 
	 * @throws IOException
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final String strPathInfo = request.getPathInfo();

		m_log.debug("Get: " + strPathInfo);

		final List<String> listElements = getObjects(strPathInfo);
		if (listElements == null || listElements.isEmpty()) {
			String strEmail = null;

			final Cookie[] aCookies = request.getCookies();
			if (aCookies != null && aCookies.length > 0) {
				for (final Cookie cookie : aCookies) {
					if (COOKIE_NAME.equals(cookie.getName())) {
						strEmail = cookie.getValue();
					}
				}
			}

			try {
				final BusinessObject boUser = getUser(strEmail, null);
				if (boUser != null) {
					final PrintWriter out = returnXml(response, HttpServletResponse.SC_OK);

					writeObject(out, boUser, false);
				}
				else {
					// no user found, so return an empty object
					writeObject(response.getWriter(), null, false);
				}
			}
			catch (final SqlBaseException e) {
				m_log.error("Database error reading the current user.", e);
				processException(response, e);
			}
		}
	}

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
				final BusinessObject boUser = getUser(strUser, strPassword);
				if (boUser != null) {
					final PrintWriter out = returnXml(response, HttpServletResponse.SC_OK);

					writeObject(out, boUser, false);
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
	 * This method parses the payload as parameters.
	 * 
	 * @param request HttpServletRequest representing the request.
	 * @throws IOException
	 */
	private Map<String, String> getParameterMap(final HttpServletRequest request) throws IOException {
		final Map<String, String> mapParameters = new HashMap<String, String>();

		final BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));

		String strLine = null;

		do {
			strLine = reader.readLine();
			if (strLine != null) {
				parseLine(strLine, mapParameters);
			}
		} while (strLine != null);

		return mapParameters;
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

	/**
	 * This method parses a set of name value pairs from the line. The line is assumed to be in the form:
	 * name1=value1&name2=value2...
	 * 
	 * @param strLine String containing the name value pairs.
	 * @param mapParameters Map where the parameters will be stored.
	 */
	private void parseLine(final String strLine, final Map<String, String> mapParameters) {
		if (strLine != null && strLine.length() > 0) {
			final StringTokenizer tokenizer = new StringTokenizer(strLine, "&");
			while (tokenizer.hasMoreTokens()) {
				final String strToken = tokenizer.nextToken();
				if (StringUtils.isNotEmpty(strToken)) {
					final int nIndex = strToken.indexOf('=');
					if (nIndex > -1) {
						final String strName = strToken.substring(0, nIndex);
						String strValue = null;

						if (nIndex < strToken.length() - 1) {
							try {
								strValue = URLDecoder.decode(strToken.substring(nIndex + 1), "UTF-8");
							}
							catch (final UnsupportedEncodingException e) {
								// should never happen
							}
						}

						mapParameters.put(strName, strValue);
					}
				}
			}
		}
	}
}

package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
			final Cookie[] aCookies = request.getCookies();
			if (aCookies != null && aCookies.length > 0) {
				for (final Cookie cookie : aCookies) {
					if (COOKIE_NAME.equals(cookie.getName())) {
						final String strValue = cookie.getValue();

						PrintWriter out;
						try {
							out = returnXml(response, HttpServletResponse.SC_OK);

							final List<FieldValue> listParameters = new ArrayList<FieldValue>();

							listParameters.add(new FieldValue("email", strValue));

							final BusinessObjectBuilder builder =
								BusinessObjectBuilderFactory.get().getBuilder("staff");
							final List<BusinessObject> listBusinessObject = builder.getAll(listParameters);

							if (listBusinessObject != null && listBusinessObject.size() == 1) {
								writeObject(out, listBusinessObject.get(0), false);
							}
							else {
								returnXml(response, HttpServletResponse.SC_NO_CONTENT);
							}
						}
						catch (final SqlBaseException e) {
							m_log.error("Database error reading the current user.", e);
							processException(response, e);
						}
					}
				}
			}
		}
	}
}

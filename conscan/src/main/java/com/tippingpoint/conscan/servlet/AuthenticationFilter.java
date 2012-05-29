package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import com.tippingpoint.utilities.StringProperties;
import com.tippingpoint.utilities.SystemProperties;

public class AuthenticationFilter implements Filter {
	public static final String AUTH_OBJECT_NAME = "auth";
	public static final String DEFAULT_TIMEOUT = "5";
	private Pattern excludePattern;

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		if (excludePattern != null && request instanceof HttpServletRequest) {
			final Matcher matcher = excludePattern.matcher(((HttpServletRequest)request).getPathInfo());
			if (matcher.matches()) {
				// URL matches exclude pattern so just forward down the chain
				chain.doFilter(request, response);
			}
			else {
				// otherwise, authenticate
				authenticate(request, response, chain);
			}
		}
		else {
			authenticate(request, response, chain);
		}
	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		final String excludePatterns = filterConfig.getInitParameter("excludePatterns");
		if (StringUtils.isNotBlank(excludePatterns)) {
			excludePattern = Pattern.compile(excludePatterns);
		}
	}

	/**
	 * This method confirms that the user is authenticated on the request.
	 * 
	 * @param request
	 * @param response
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	private void authenticate(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		// Deal with the session
		final HttpServletRequest httpRequest = (HttpServletRequest)request;
		final HttpSession session = httpRequest.getSession();
		if (session != null) {
			final long currTime = System.currentTimeMillis();
			final Long lTimeLastAccessed = (Long)session.getAttribute(AUTH_OBJECT_NAME);
			if (lTimeLastAccessed != null) {
				final long lElapsedTime = currTime - lTimeLastAccessed.longValue();
				final StringProperties sp = SystemProperties.getSystemProperties().getStringProperties();
				final String strTimeout = sp.getValue("authentication.timeout.minutes");
				final int intAuthTimeout =
					Integer.valueOf(strTimeout == null ? DEFAULT_TIMEOUT : strTimeout).intValue() * 60 * 1000;
				final int intCookieTimeout =
					Integer.valueOf(strTimeout == null ? DEFAULT_TIMEOUT : strTimeout).intValue() * 60;
				if (lElapsedTime > intAuthTimeout) {
					session.setAttribute(AUTH_OBJECT_NAME, null);
					System.out.println("EXPIRED AUTH OBJECT: " + session.getId());
					final HttpServletResponse httpResponse = (HttpServletResponse)response;
					if (httpResponse != null) {
						httpResponse.sendError(401);
					}
				}
				else {
					final HttpServletResponse httpResponse = (HttpServletResponse)response;
					if (httpResponse != null) {
						User.setCookieExpiration(httpRequest, httpResponse, intCookieTimeout);
					}

					session.setAttribute(AUTH_OBJECT_NAME, System.currentTimeMillis());

					System.out.println("AUTH object: " + lTimeLastAccessed.toString() + " sessionId: " +
							session.getId());
					chain.doFilter(request, response);
				}
			}
			else {
				System.out.println("NO AUTH OBJECT: " + session.getId());
				final HttpServletResponse httpResponse = (HttpServletResponse)response;
				if (httpResponse != null) {
					User.setCookieExpiration(httpRequest, httpResponse, 0);
					httpResponse.sendError(401);
				}

				System.out.println("DONE 401: " + session.getId());
			}
		}
	}
}

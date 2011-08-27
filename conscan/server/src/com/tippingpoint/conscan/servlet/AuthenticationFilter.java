package com.tippingpoint.conscan.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthenticationFilter implements Filter {

	public static final String AUTH_OBJECT_NAME = "auth";
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
	    chain.doFilter(request, response);
	    
	    // Deal with the session
	    HttpSession session = ((HttpServletRequest)request).getSession();
	    if (session != null) {
			long currTime = System.currentTimeMillis();
	    	Long lTimeLastAccessed = (Long)session.getAttribute(AUTH_OBJECT_NAME);
	    	if (lTimeLastAccessed != null) {
	    		long lElapsedTime = currTime - lTimeLastAccessed.longValue();
	    		if (lElapsedTime > 30000) {
	    			session.setAttribute(AUTH_OBJECT_NAME, null);
	    			System.out.println("EXPIRED AUTH OBJECT");
	    			HttpServletResponse httpResponse = (HttpServletResponse)response;
	    			if (httpResponse != null) {
	    				httpResponse.sendError(401);
	    			}
	    		}
	    		else { 
	    			session.setAttribute(AUTH_OBJECT_NAME, System.currentTimeMillis());
	    			System.out.println("AUTH object: " + lTimeLastAccessed.toString());
	    		}
	    	}
	    	else {
	    		System.out.println("NO AUTH OBJECT");
	    	}
	    }
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}

package com.tippingpoint.conscan.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LogCommentService extends BaseTableService {
	private static Log m_log = LogFactory.getLog(BaseTableService.class);
	private static final long serialVersionUID = 2318703620281811532L;

	public LogCommentService() {
		super("logcomment");
	}
	
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		m_log.debug("########## LogCommentService:doPost()");
	}	
}

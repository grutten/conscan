package com.tippingpoint.conscan.servlet;

import java.io.File;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;
import com.sun.jersey.spi.resource.Singleton;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;

@Singleton
@Path("/reports")
public class ReportsService {
	@Context
	protected ServletContext context;

	/**
	 * This method returns the security check report.
	 */
	@GET
	@Path("/security")
	@Produces(MediaType.TEXT_HTML)
	public StreamingOutput security() {
		return new ReportOutput("securitycheck.jasper");
	}

	/**
	 * This class is used to generate the output of the report.
	 */
	private class ReportOutput implements StreamingOutput {
		/** This member holds the name of the file. */
		private File m_fileReport;

		/**
		 * This method constructs the report generator for the named report.
		 * 
		 * @param strReportName String containing the name of the report.
		 */
		public ReportOutput(final String strReportName) {
			m_fileReport = new File(context.getRealPath("reports/" + strReportName));

			if (!m_fileReport.exists()) {
				throw new IllegalArgumentException("Could not find specified report '" + strReportName + "'.");
			}

			System.out.println("File: " + m_fileReport.getAbsolutePath());
		}

		/**
		 * This method returns the detailed output of the report.
		 */
		@Override
		public void write(final OutputStream out) {
			try {
				final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(m_fileReport);

				final Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("ReportTitle", "Address Report");
				parameters.put("BaseDir", m_fileReport.getParentFile());
				
				parameters.put("Title", "Security Report");
				parameters.put("PrintedBy", "Joe Officer");
				parameters.put("PrintedDate", new Date().toString());

				final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
				Connection connection = manager.getConnection();

				final JasperPrint jasperPrint =
					JasperFillManager.fillReport(jasperReport, parameters, connection);

				final JRHtmlExporter exporter = new JRHtmlExporter();

				// request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);

				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/conscan/reports/securitycheck.html_files/");

				exporter.exportReport();
			}
			catch (final JRException e) {
				throw new IllegalStateException(e);
			}
			catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}

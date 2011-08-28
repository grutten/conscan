package com.tippingpoint.conscan.reports;

import java.io.File;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.StreamingOutput;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;

/**
 * This class is the base class for the reports.
 */
public abstract class ReportBase {
	@Context
	protected ServletContext context;

	/**
	 * This class is used to generate the output of the report.
	 */
	protected class ReportOutput implements StreamingOutput {
		/** This member holds the name of the file. */
		private final File m_fileReport;

		/** This method holds the parameters needed to complete the report. */
		private final Map<String, Object> m_mapParameters;

		/**
		 * This method constructs the report generator for the named report.
		 * 
		 * @param strReportName String containing the name of the report.
		 */
		public ReportOutput(final String strReportName, final Map<String, Object> mapParameters) {
			m_fileReport = new File(context.getRealPath("reports/" + strReportName));

			if (!m_fileReport.exists()) {
				throw new IllegalArgumentException("Could not find specified report '" + strReportName + "'.");
			}

			System.out.println("File: " + m_fileReport.getAbsolutePath());

			m_mapParameters = mapParameters;
		}

		/**
		 * This method returns the detailed output of the report.
		 */
		@Override
		public void write(final OutputStream out) {
			try {
				final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(m_fileReport.getPath());

				final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
				final Connection connection = manager.getConnection();

				final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, m_mapParameters, connection);

				final JRHtmlExporter exporter = new JRHtmlExporter();

				// request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);

				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);

				final String strContextPath = context.getContextPath();

				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, strContextPath +
						"/reports/securitycheck.html_files/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, false);

				exporter.exportReport();
			}
			catch (final JRException e) {
				throw new IllegalStateException(e);
			}
			catch (final SQLException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}

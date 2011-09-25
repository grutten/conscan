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
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;

/**
 * This class is the base class for the reports.
 */
public abstract class ReportBase {
	private static final String JR_COMPILED_EXT = ".jasper";
	private static final String JR_RAW_EXT = ".jrxml";

	private static Log m_log = LogFactory.getLog(ReportBase.class);

	@Context
	protected ServletContext context;

	/** This member holds the name of the file. */
	private File m_fileReport;

	/** This member holds the name of the report. */
	private String m_strName;

	/**
	 * This method constructs a new named report.
	 * 
	 * @param strName String containing the name of the report.
	 */
	public ReportBase(final String strName) {
		m_strName = strName;
	}

	/**
	 * This method uses the local report file to return a stream generating the report.
	 * 
	 * @param mapParameters Map<String, Object> instance containing parameters sent directly to the report.
	 */
	protected StreamingOutput generateReportStreamPdf(final Map<String, Object> mapParameters) {
		ensureReportCompiled();

		return new ReportOutputPdf(mapParameters);
	}

	/**
	 * This method uses the local report file to return a stream generating the report.
	 * 
	 * @param mapParameters Map<String, Object> instance containing parameters sent directly to the report.
	 */
	protected StreamingOutput generateReportStreamHtml(final Map<String, Object> mapParameters) {
		ensureReportCompiled();

		return new ReportOutputHtml(mapParameters);
	}

	/**
	 * This method compiles the report if needed.
	 */
	private void compileReport() {
		if (m_strName == null || m_strName.length() == 0) {
			throw new IllegalArgumentException("Report name not specified.");
		}

		File fileReport = new File(context.getRealPath("reports/"), m_strName);
		if (!fileReport.exists()) {
			throw new IllegalArgumentException("Could not find specified report '" + m_strName + "'.");
		}

		String strExtension = JR_RAW_EXT;
		final int nExtenationIndex = m_strName.lastIndexOf('.');
		if (nExtenationIndex > 0) {
			strExtension = m_strName.substring(nExtenationIndex);
		}

		if (JR_RAW_EXT.equalsIgnoreCase(strExtension)) {
			final File fileCompiled =
				new File(fileReport.getParent(), (nExtenationIndex > 0 ? m_strName.substring(0, nExtenationIndex) 
						: m_strName) + JR_COMPILED_EXT);
			if (fileCompiled.exists() && fileCompiled.lastModified() < fileReport.lastModified() ||
					!fileCompiled.exists()) {
				// compile the report
				try {
					m_strName = JasperCompileManager.compileReportToFile(fileReport.getAbsolutePath());
					fileReport = new File(m_strName);
				}
				catch (final JRException e) {
					fileReport = null;
					m_log.error("Error compiling report", e);
				}
			}
			else {
				fileReport = fileCompiled;
			}
		}

		m_fileReport = fileReport;

		if (!m_fileReport.exists()) {
			throw new IllegalArgumentException("Could not find specified report '" + m_fileReport + "'.");
		}

		System.out.println("File: " + m_fileReport.getAbsolutePath());
	}

	/**
	 * This method makes sure the report is compiled.
	 */
	private void ensureReportCompiled() {
		if (m_fileReport == null) {
			compileReport();
		}
	}

	/**
	 * This class is used to generate the output of the report.
	 */
	protected abstract class ReportOutput implements StreamingOutput {
		/** This method holds the parameters needed to complete the report. */
		private final Map<String, Object> m_mapParameters;

		/**
		 * This method constructs the report generator for the named report.
		 * 
		 * @param strReportName String containing the name of the report.
		 */
		public ReportOutput(final Map<String, Object> mapParameters) {
			m_mapParameters = mapParameters;
		}
		
		/**
		 * This method constructs an instance of the report.
		 * @throws JRException 
		 * @throws SQLException 
		 */
		protected JasperPrint getReport() throws JRException, SQLException {
			final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(m_fileReport);

			final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
			final Connection connection = manager.getConnection();

			return JasperFillManager.fillReport(jasperReport, m_mapParameters, connection);
		}
	}
	
	/**
	 * This class output an HTML report.
	 */
	protected class ReportOutputHtml extends ReportOutput {
		/**
		 * This method constructs the report generator.
		 * 
		 * @param strReportName String containing the name of the report.
		 */
		public ReportOutputHtml(final Map<String, Object> mapParameters) {
			super(mapParameters);
		}

		/**
		 * This method returns the detailed output of the report.
		 */
		@Override
		public void write(final OutputStream out) {
			try {
				final JRHtmlExporter exporter = new JRHtmlExporter();

				exporter.setParameter(JRExporterParameter.JASPER_PRINT, getReport());
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

	/**
	 * This class output an HTML report.
	 */
	protected class ReportOutputPdf extends ReportOutput {
		/**
		 * This method constructs the report generator.
		 * 
		 * @param strReportName String containing the name of the report.
		 */
		public ReportOutputPdf(final Map<String, Object> mapParameters) {
			super(mapParameters);
		}

		/**
		 * This method returns the detailed output of the report.
		 */
		@Override
		public void write(final OutputStream out) {
			try {
				final JRPdfExporter exporter = new JRPdfExporter();

				exporter.setParameter(JRExporterParameter.JASPER_PRINT, getReport());
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

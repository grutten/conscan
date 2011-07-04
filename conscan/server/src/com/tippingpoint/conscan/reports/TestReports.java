package com.tippingpoint.conscan.reports;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import com.tippingpoint.test.TestCommonCase;

public class TestReports extends TestCommonCase {
	/**
	 * This method test the simple hello world type of report.
	 */
	public void testHelloWorld() {
		try {
			File file = new File("server/WebContent/reports");
			System.out.println("Default folder: " + file.getAbsolutePath());
			
			JasperReport jasperReport = JasperCompileManager.compileReport(new File(file, "helloworld.jrxml").toString());
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<String, Object>(), new JREmptyDataSource());
			JasperExportManager.exportReportToPdfFile(jasperPrint, new File(file, "helloworld.pdf").toString());
		}
		catch (final JRException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method tests a slightly more complicated report.
	 */
	public void test2() {
		File file = new File("server/WebContent/reports");
		System.out.println("Default folder: " + file.getAbsolutePath());

		try {
			// First, load JasperDesign from XML and compile it into JasperReport
			JasperReport jasperReport = JasperCompileManager.compileReport(new File(file, "test2.jrxml").toString());
			
			// Second, create a map of parameters to pass to the report.
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("ReportTitle", "Basic JasperReport");
			parameters.put("MaxSalary", new Double(25000.00));

			// Third, get a database connection
//			Connection conn = Database.getConnection(); 

			// Fourth, create JasperPrint using fillReport() method
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
			
			// You can use JasperPrint to create PDF
			JasperExportManager.exportReportToPdfFile(jasperPrint, new File(file, "test2.pdf").toString());

			// Or to view report in the JasperViewer
			JasperViewer.viewReport(jasperPrint);
		}
		catch (JRException e) {
			e.printStackTrace();
			fail();
		} finally {}
	}
}

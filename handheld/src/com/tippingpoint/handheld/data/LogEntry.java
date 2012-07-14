package com.tippingpoint.handheld.data;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class LogEntry {
	private Activity m_activity;
	private Location m_location;
	private Offender m_offender;
	private ComplianceValue m_complianceValue;
	private String m_strLocationName;
	private String m_strStaffId;
	private String m_strDateCreated;
	private String m_strScannedBarcode;
	
	private FileWriter m_logOutputStream;
	
	LogEntry() {
		if (m_logOutputStream == null)
			try {
				initialize();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public Activity getActivity() { return m_activity; }
	public ComplianceValue getComplianceValue() { return m_complianceValue; }
	public String getDateCreated() { return m_strDateCreated; }
	public Location getLocation() { return m_location; }
	public Offender getOffender() { return m_offender; }
	public String getLocationName() { return m_strLocationName; }
	public String getScannedBarcode() { return m_strScannedBarcode; }
	public String getStaffId() { return m_strStaffId; }
	
	public void setActivity(Activity activity) { m_activity = activity; }
	public void setComplianceValue(ComplianceValue complianceValue) { m_complianceValue = complianceValue; }
	public void setDateCreated(String strDateCreated) { m_strDateCreated = strDateCreated; }
	public void setLocation(Location location) { m_location = location; }
//	public void setLocationName(String strName) { m_strLocationName = strName; }
	public void setOffender(Offender offender) { m_offender = offender; }
	public void setScannedBarcode(String strScannedBarcode) { m_strScannedBarcode = strScannedBarcode; }
	public void setStaffId(String strStaffId) { m_strStaffId = strStaffId; }

	
	public void clear() {
		m_activity = null;
		m_location = null;
		m_offender = null;
		m_complianceValue = null;
		m_strLocationName = null;
		m_strStaffId = null;
		m_strDateCreated = null;
		
	}

	public void close() throws IOException {
		if (m_logOutputStream != null)
			m_logOutputStream.close();
	}
	
	public void initialize() throws IOException {
		Calendar c = Calendar.getInstance();
		
		// format - yyyymmdd24mmss
        String strFilename = Integer.toString(c.get(Calendar.YEAR)) +
        	Integer.toString(c.get(Calendar.MONTH) + 1) + 
        	Integer.toString(c.get(Calendar.DATE)) + 
        	Integer.toString(c.get(Calendar.HOUR_OF_DAY)) +
        	Integer.toString(c.get(Calendar.MINUTE)) + 
        	Integer.toString(c.get(Calendar.SECOND));

        	m_logOutputStream = new FileWriter("\\My Documents\\_log" + strFilename + ".xml");
        	
        	if (m_logOutputStream != null)
        		// NOTE: because the handheld is not designed to recognize when
        		// this file is complete, the server must provide the closing 
        		// </objects> tag
       		m_logOutputStream.write("<objects>\n");
	}
	
	public void write() throws IOException {
		m_logOutputStream.write("<object name='scannerlog'>\n");
		writeTag("staffid", getStaffId());
		writeTag("created", getDateCreated());
		writeTag("barcode", getScannedBarcode());
		writeTag("activityid", m_activity.getActivityId());
		if (getOffender() != null)
			writeTag("offenderid", getOffender().getOffenderId());
		if (getLocation() != null)
			writeTag("locationid", getLocation().getLocationId());
		writeTag("compliancevalueid", getComplianceValue().getComplianceValueId());
		m_logOutputStream.write("</object>\n");
		m_logOutputStream.flush(); 
	}
	
	
	private  void writeTag(String strTagName, String strValue) throws IOException {
		m_logOutputStream.write("\t<field name='" + strTagName + "'>" + strValue + "</field>\n");
	}
}

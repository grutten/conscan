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
	
	private FileWriter m_logOutputStream;
	
	LogEntry() {
		Calendar c = Calendar.getInstance();
		
		// format - yyyymmdd24mmss
        String strFilename = Integer.toString(c.get(Calendar.YEAR)) +
        	Integer.toString(c.get(Calendar.MONTH) + 1) + 
        	Integer.toString(c.get(Calendar.DATE)) + 
        	Integer.toString(c.get(Calendar.HOUR_OF_DAY)) +
        	Integer.toString(c.get(Calendar.MINUTE)) + 
        	Integer.toString(c.get(Calendar.SECOND));

        try {
        	m_logOutputStream = new FileWriter("\\My Documents\\log" + strFilename + ".xml");
        }
        catch (Exception e) {
        	// TODO: eat the file exception
        }
	}
	
	public Activity getActivity() { return m_activity; }
	public ComplianceValue getComplianceValue() { return m_complianceValue; }
	public String getDateCreated() { return m_strDateCreated; }
	public Location getLocation() { return m_location; }
	public Offender getOffender() { return m_offender; }
	public String getLocationName() { return m_strLocationName; }
	public String getStaffId() { return m_strStaffId; }
	
	public void setActivity(Activity activity) { m_activity = activity; }
	public void setComplianceValue(ComplianceValue complianceValue) { m_complianceValue = complianceValue; }
	public void setDateCreated(String strDateCreated) { m_strDateCreated = strDateCreated; }
	public void setLocation(Location location) { m_location = location; }
//	public void setLocationName(String strName) { m_strLocationName = strName; }
	public void setOffender(Offender offender) { m_offender = offender; }
	public void setStaffId(String strStaffId) { m_strStaffId = strStaffId; }

	
	public void clean() {
		m_activity = null;
		m_location = null;
		m_offender = null;
		m_complianceValue = null;
		m_strLocationName = null;
		m_strStaffId = null;
		m_strDateCreated = null;
		
	}
	
	public void write() throws IOException {
		m_logOutputStream.write("<entry>\n");
		writeTagWithId("officer", "999", true);
		writeTag("created", getDateCreated());
		writeTagWithId("activity", m_activity.getActivityId(), true);
		if (getOffender() != null) {
			m_logOutputStream.write("<offender>\n");
			writeTag("bookingnumber", getOffender().getBookingNumber());
			writeTagWithId("compliance", getComplianceValue().getCompliancevalueId(), true);
			m_logOutputStream.write("</offender>\n");
		}
		if (getLocation() != null) {
			m_logOutputStream.write("<location>\n");
			writeTag("locationid", getLocation().getLocationId());
			writeTagWithId("compliance", getComplianceValue().getCompliancevalueId(), true);
			m_logOutputStream.write("</location>\n");
		}
		m_logOutputStream.write("</entry>\n");
		m_logOutputStream.flush(); 
	}
	
	private  void writeTag(String strTagName, String strValue) throws IOException {
		m_logOutputStream.write("<" + strTagName + ">" + strValue + "</" + strTagName + ">\n");
	}
	
	private  void writeTagWithId(String strTagName, String strValueForIdAttr, boolean bPrintClosingTag) throws IOException {
		m_logOutputStream.write("<" + strTagName + " " + strTagName + "id='" + strValueForIdAttr + "'>");
		if (bPrintClosingTag)
			m_logOutputStream.write("</" + strTagName + ">");
		m_logOutputStream.write("\n");
		
	}
	
}

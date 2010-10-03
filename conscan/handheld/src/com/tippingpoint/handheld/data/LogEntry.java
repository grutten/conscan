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
		m_logOutputStream.write("<object name='log'>\n");
		writeTag("staffid", "999");
		writeTag("created", getDateCreated());
		writeTag("activityid", m_activity.getActivityId());
		if (getOffender() != null)
			writeTag("offenderid", getOffender().getBookingNumber());
		if (getLocation() != null)
			writeTag("locationid", getLocation().getLocationId());
		writeTag("compliancevalueid", getComplianceValue().getCompliancevalueId());
		m_logOutputStream.write("</object>\n");
		m_logOutputStream.flush(); 
	}
	
	private  void writeTag(String strTagName, String strValue) throws IOException {
		m_logOutputStream.write("\t<field name='" + strTagName + "'>" + strValue + "</field>\n");
	}
}

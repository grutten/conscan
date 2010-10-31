package com.tippingpoint.handheld.data;

public class Activity {
	private String m_strActivityId;

	private String m_strName;
	private String m_strComplianceId;
	private String m_strScanType;
	private String m_strComplianceType;
	
	public String getComplianceId() { return m_strComplianceId; }
	public String getCompliancetype() { return m_strComplianceType; }
	public String getActivityId() { return m_strActivityId; }
	public String getName() { return m_strName; }
	public String getScantype() { return m_strScanType; }
	public boolean isCellScan() { return "1".equalsIgnoreCase(m_strScanType); }
	public boolean isOffenderScan() {return "2".equalsIgnoreCase(m_strScanType); }
	public boolean isCellCompliance() { return "1".equalsIgnoreCase(m_strComplianceType); }
	public boolean isOffenderCompliance() { return "2".equalsIgnoreCase(m_strComplianceType); }
	public void setComplianceId(String strId) { m_strComplianceId = strId; }
	public void setCompliancetype(String strCompliancetype) { m_strComplianceType = strCompliancetype; }
	public void setActivityId(String strId) { m_strActivityId = strId; } 
	public void setName(String strName) { m_strName = strName; }
	public void setScantype(String strScantype) { m_strScanType = strScantype; }
	
	public String toString() { return getName(); }
}

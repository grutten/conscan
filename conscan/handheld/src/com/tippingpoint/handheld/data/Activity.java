package com.tippingpoint.handheld.data;

public class Activity {
	private String m_strName;
	private String m_strComplianceId;
	private String m_strScanType;
	
	public String getComplianceId() { return m_strComplianceId; }
	public String getName() { return m_strName; }
	public String getScantype() { return m_strScanType; }
	public boolean isCellScan() { return "1".equalsIgnoreCase(m_strScanType); }
	public boolean isOffenderScan() {return "2".equalsIgnoreCase(m_strScanType); }
	public void setComplianceId(String strId) { m_strComplianceId = strId; }
	public void setName(String strName) { m_strName = strName; }
	public void setScantype(String strScantype) { m_strScanType = strScantype; }
}

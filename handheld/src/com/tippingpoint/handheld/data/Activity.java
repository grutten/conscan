package com.tippingpoint.handheld.data;

public class Activity implements DisplayOrderInterface {
	private static final String COMPLIANCETYPE_OFFENDER = "1";
	private static final String COMPLIANCETYPE_CELL = "2";
	private static final String SCANTYPE_OFFENDER = "1";
	private static final String SCANTYPE_CELL = "2";
	
	
	private String m_strActivityId;

	private String m_strName;
	private String m_strComplianceId;
	private String m_strScanType;
	private String m_strComplianceType;
	private String m_strDisplayOrder;
	
	public String getActivityId() { return m_strActivityId; }
	public String getComplianceId() { return m_strComplianceId; }
	public String getCompliancetype() { return m_strComplianceType; }
	public String getDisplayOrder() { return m_strDisplayOrder; }
	public String getName() { return m_strName; }
	public String getScantype() { return m_strScanType; }
	public boolean isCellCompliance() { return COMPLIANCETYPE_CELL.equalsIgnoreCase(m_strComplianceType); }
	public boolean isCellScan() { return SCANTYPE_CELL.equalsIgnoreCase(m_strScanType); }
	public boolean isOffenderCompliance() { return COMPLIANCETYPE_OFFENDER.equalsIgnoreCase(m_strComplianceType); }
	public boolean isOffenderScan() {return SCANTYPE_OFFENDER.equalsIgnoreCase(m_strScanType); }
	public void setActivityId(String strId) { m_strActivityId = strId; } 
	public void setComplianceId(String strId) { m_strComplianceId = strId; }
	public void setCompliancetype(String strCompliancetype) { m_strComplianceType = strCompliancetype; }
	public void setDisplayOrder(String strDisplayOrder) { m_strDisplayOrder = strDisplayOrder; }
	public void setName(String strName) { m_strName = strName; }
	public void setScantype(String strScantype) { m_strScanType = strScantype; }
	
	public String toString() { return getName(); }
}

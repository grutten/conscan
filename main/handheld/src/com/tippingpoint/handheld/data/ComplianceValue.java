package com.tippingpoint.handheld.data;

public class ComplianceValue implements DisplayOrderInterface {
	private String m_strComplianceValueId;
	
	private String m_strComplianceId;
	private String m_strDisplayOrder;
	private String m_strValue;
	private boolean m_bDefault = false;
	
	public String getComplianceId() { return m_strComplianceId; }
	public String getComplianceValueId() { return m_strComplianceValueId; }
	public boolean getDefault() { return m_bDefault; }
	public String getDisplayOrder() { return m_strDisplayOrder; }
	public boolean getIsDefault() { return m_bDefault; }
	public String getValue() { return m_strValue; }
	
	public void setComplianceId(String strId) { m_strComplianceId = strId; }
	public void setComplianceValueId(String strId) { m_strComplianceValueId = strId; }
	public void setDefault(boolean b) { m_bDefault = b; }  // ??? should this be deprecated or do we continue to support???
	public void setDisplayOrder(String strDisplayOrder) { m_strDisplayOrder = strDisplayOrder; }
	public void setIsDefault(String strDefault) { m_bDefault = "true".equalsIgnoreCase(strDefault); }
	public void setValue(String strValue) { m_strValue = strValue; }
	
	public String toString() { return getValue(); }
}

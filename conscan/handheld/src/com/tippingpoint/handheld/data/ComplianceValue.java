package com.tippingpoint.handheld.data;

public class ComplianceValue {
	private String m_strComplianceValueId;
	private String m_strValue;
	private boolean m_bDefault = false;
	
	public String getCompliancevalueId() { return m_strComplianceValueId; }
	public boolean getDefault() { return m_bDefault; }
	public String getValue() { return m_strValue; }
	
	public void setComplianceValueId(String strId) { m_strComplianceValueId = strId; }
	public void setDefault(boolean b) { m_bDefault = b; }
	public void setValue(String strValue) { m_strValue = strValue; }
}

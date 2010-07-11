package com.tippingpoint.handheld.data;

import java.util.ArrayList;

public class Compliance {
	private String m_strComplianceId;
	private String m_strName;
	private ArrayList m_listValues = new ArrayList();
	
	public void addValue(ComplianceValue complianceValue) { m_listValues.add(complianceValue); }
	public String getComplianceId() { return m_strComplianceId; }
	public String getName() { return m_strName; }
	public ArrayList getValues() { return m_listValues; }
	public void setComplianceId(String strId) { m_strComplianceId = strId; }
	public void setName(String strName) { m_strName = strName; }
}

package com.tippingpoint.handheld.data;

import java.util.ArrayList;

public class ComplianceConfiguration {
	private String m_strId;
	private String m_strName;
	private ArrayList m_listValues = new ArrayList();
	
	public void addValue(String strValue) { m_listValues.add(strValue); }
	public String getId() { return m_strId; }
	public String getName() { return m_strName; }
	public ArrayList getValues() { return m_listValues; }
	public void setId(String strId) { m_strId = strId; }
	public void setName(String strName) { m_strName = strName; }
}

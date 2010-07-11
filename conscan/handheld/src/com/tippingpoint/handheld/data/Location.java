package com.tippingpoint.handheld.data;

import java.util.ArrayList;

//import java.util.ArrayList;

public class Location {
	private String m_strLocationId;
	private String m_strName;
	private String m_strBarcode;
	private ArrayList m_listOffenders;
	
	public String getBarcode() { return m_strBarcode; }
	public String getLocationId() { return m_strLocationId; }
	public String getName() { return m_strName; }
	public ArrayList getOffenders() { return m_listOffenders; }
	
	public void setBarcode(String strBarcode) { m_strBarcode = strBarcode; }
	public void setLocationId(String strId) { m_strLocationId = strId; }
	public void setName(String strName) { m_strName = strName; }
	public void setOffenders(ArrayList list) { m_listOffenders = list; }

}

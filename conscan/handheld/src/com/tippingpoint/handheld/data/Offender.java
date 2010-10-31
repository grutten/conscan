package com.tippingpoint.handheld.data;

public class Offender {
	private String m_strOffenderId;
	private String m_strFirstName;
	private String m_strLastName;
	private String m_strBookingNumber;
	private String m_strBarcode;
	
	public String getBarcode() { return m_strBarcode; }
	public String getBookingNumber() { return m_strBookingNumber; }
	public String getFirstName() { return m_strFirstName; }
	public String getLastName() { return m_strLastName; }
	public String getOffenderId() { return m_strOffenderId; }
	public String getName() { return getLastName(); }  // @deprecate
	
	public void setBarcode(String strBarcode) { m_strBarcode = strBarcode; }
	public void setBookingNumber(String strBookingNumber) { m_strBookingNumber = strBookingNumber; } 
	public void setFirstName(String strName) { m_strFirstName = strName; }
	public void setLastName(String strName) { m_strLastName = strName; }
	public void setName(String strName) { setLastName(strName); }  // @deprecate
	public void setOffenderId(String strId) { m_strOffenderId = strId; } 

	public String toString() { return getLastName() + ", " + getFirstName(); }
}

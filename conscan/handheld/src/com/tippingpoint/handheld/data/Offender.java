package com.tippingpoint.handheld.data;

public class Offender {
	private String m_strName;
	private String m_strBookingNumber;
	private String m_strBarcode;
	
	public String getBarcode() { return m_strBarcode; }
	public String getBookingNumber() { return m_strBookingNumber; }
	public String getName() { return m_strName; }
	
	public void setBarcode(String strBarcode) { m_strBarcode = strBarcode; }
	public void setBookingNumber(String strBookingNumber) { m_strBookingNumber = strBookingNumber; } 
	public void setName(String strName) { m_strName = strName; }

}
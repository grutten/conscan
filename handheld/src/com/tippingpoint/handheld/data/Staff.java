package com.tippingpoint.handheld.data;

public class Staff {
	private String m_strStaffId;
	private String m_strFirstName;
	private String m_strLastName;
	private String m_strBadgeNumber;
	private String m_strEmail;
	private String m_strPassword;
	private String m_strCreated;
	private String m_strModified;
	
	public String getStaffId() { return m_strStaffId; }
	public String getFirstName() { return m_strFirstName; }
	public String getLastName() { return m_strLastName; }
	public String getBadgeNumber() { return m_strBadgeNumber; }
	public String getEmail() { return m_strEmail; }
	public String getPassword() { return m_strPassword; }
	public String getCreated() { return m_strCreated; }
	public String getModified() { return m_strModified; }
	
	public void setStaffId(String str) { m_strStaffId = str; }
	public void setFirstName(String str) { m_strFirstName = str; }
	public void setLastName(String str) { m_strLastName = str; }
	public void setBadgeNumber(String str) { m_strBadgeNumber = str; }
	public void setEmail(String str) {  m_strEmail = str; }
	public void setPassword(String str) {  m_strPassword = str; }
	public void setCreated(String str) {  m_strCreated = str; }
	public void setModified(String str) {  m_strModified= str; }
}

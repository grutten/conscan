package com.tippingpoint.handheld.data;

import com.tippingpoint.handheld.ui.DataChoice;

/**
 * This class is meant to associate compliance information with a scannable
 * (e.g. an offender or a location).
 * @author mgee
 *
 */
public class Scannable {
	DataChoice m_dataChoice;
	Object m_object;
	
	public Scannable() {
		m_dataChoice = new DataChoice();  // TODO: check for garbage collection
	}

	public void clearComplianceControl() { m_dataChoice = null; } 
	public DataChoice getComplianceControl() { return m_dataChoice; }
	public Object getObject() { return m_object; }
	public void setObject(Object o) { m_object = o; }
}

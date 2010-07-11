package com.tippingpoint.handheld.ui;

import java.awt.Choice;
import java.util.HashMap;

public class DataChoice extends Choice {
	private HashMap m_hashItems = new HashMap();
	
	DataChoice() {
		super();
	}
	
	public void add(String strValue, Object data) {
		super.add(strValue);
		
		m_hashItems.put(strValue, data);
	}
	
	public Object getSelectedItemObject() {
		String strKey = super.getSelectedItem();
		
		return m_hashItems.get(strKey);
	}
}

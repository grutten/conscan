package com.tippingpoint.app.client;

import com.google.gwt.user.client.ui.VerticalPanel;

public class ScreenBase {
	protected VerticalPanel m_panelMain = new VerticalPanel();
	
	public ScreenBase() {
	    m_panelMain.setWidth("600px");
	    m_panelMain.setHeight("400px");
	}

	public VerticalPanel getMainPanel() { return m_panelMain; }
	
}

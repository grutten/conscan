package com.tippingpoint.app.client;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class RoughCutMenu implements ScreenInterface {

    private TabLayoutPanel m_tabPanel = new TabLayoutPanel(1.5, Unit.EM);
	private VerticalPanel m_panelMain = new VerticalPanel();

	RoughCutMenu() {
		m_tabPanel.add(new Label("NOT IMPLEMENTED"), "Import");
		m_tabPanel.add(new Label("NOT IMPLEMENTED"), "Configure");
		m_tabPanel.add(new Label("NOT IMPLEMENTED"), "Update");
		m_tabPanel.add(new Label("NOT IMPLEMENTED"), "Scanner");
		m_tabPanel.add(new Label("NOT IMPLEMENTED"), "Monitor");
		m_tabPanel.add(new Label("NOT IMPLEMENTED"), "Reports");
		
	    m_tabPanel.setWidth("640px");
	    m_tabPanel.setHeight("480px");
	    m_tabPanel.selectTab(0);
	    
		// Assemble Main panel.
	    m_panelMain.add(m_tabPanel);
	}
	
	@Override
	public VerticalPanel getMainPanel() { return m_panelMain; }
}

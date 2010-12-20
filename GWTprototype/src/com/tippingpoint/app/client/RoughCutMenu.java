package com.tippingpoint.app.client;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.tippingpoint.app.client.configure.Configure;
import com.tippingpoint.app.client.importexport.ImportExport;
import com.tippingpoint.app.client.monitor.Monitor;
import com.tippingpoint.app.client.report.Report;
import com.tippingpoint.app.client.scanner.Scanner;
import com.tippingpoint.app.client.update.Update;

public class RoughCutMenu implements ScreenInterface {

    private TabLayoutPanel m_tabPanel = new TabLayoutPanel(1.5, Unit.EM);
	private VerticalPanel m_panelMain = new VerticalPanel();

	RoughCutMenu() {
		m_tabPanel.add(new ImportExport().getMainPanel(), "Import");
		m_tabPanel.add(new Configure().getMainPanel(), "Configure");
		m_tabPanel.add(new Update().getMainPanel(), "Update");
		m_tabPanel.add(new Scanner().getMainPanel(), "Scanner");
		m_tabPanel.add(new Monitor().getMainPanel(), "Monitor");
		m_tabPanel.add(new Report().getMainPanel(), "Reports");
		
	    m_tabPanel.setWidth("640px");
	    m_tabPanel.setHeight("480px");
	    m_tabPanel.selectTab(0);
	    
		// Assemble Main panel.
	    m_panelMain.add(m_tabPanel);
	    
	}
	
	@Override
	public VerticalPanel getMainPanel() { return m_panelMain; }
}

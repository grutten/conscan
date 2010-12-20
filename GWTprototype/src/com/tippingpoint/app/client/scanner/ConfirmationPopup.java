package com.tippingpoint.app.client.scanner;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ConfirmationPopup extends PopupPanel {
	private VerticalPanel m_panel = new VerticalPanel();
	private FlexTable stocksFlexTable = new FlexTable();
	private Button addStockButton = new Button("Confirm");
	
	public ConfirmationPopup() {
    	
        // PopupPanel's constructor takes 'auto-hide' as its boolean parameter.
        // If this is set, the panel closes itself automatically when the user
        // clicks outside of it.
        super(true);
        
        setWidth("600px");
        setHeight("400px");

        // PopupPanel is a SimplePanel, so you have to set it's widget property to
        // whatever you want its contents to be.
        setWidget(m_panel);
        
		// Create table for stock data.
	    stocksFlexTable.setText(0, 0, "Location");
	    stocksFlexTable.setText(0, 1, "Activity");
	    stocksFlexTable.setText(0, 2, "Offender");
	    stocksFlexTable.setText(0, 3, "Compliance");
	    
	    populateFlexTable(stocksFlexTable);
	    
	    // Add styles to elements in the stock list table.
	    stocksFlexTable.getRowFormatter().addStyleName(0, "logEntryListHeader");
	    stocksFlexTable.addStyleName("logEntryList");
	    stocksFlexTable.getCellFormatter().addStyleName(0, 1, "logEntryListNumericColumn");
	    stocksFlexTable.getCellFormatter().addStyleName(0, 2, "logEntryListNumericColumn");
	    stocksFlexTable.getCellFormatter().addStyleName(0, 3, "logEntryListRemoveColumn");
	    
		// Assemble Add Stock panel.
	    m_panel.add(stocksFlexTable);
	    m_panel.add(addStockButton);
	    m_panel.addStyleName("addPanel");	    
	}

    void populateFlexTable(FlexTable stocksFlexTable) {
    	for (int i = 1; i < 10; ++i) {
	    	stocksFlexTable.setText(i, 0, "T162-A-012L");
	    	stocksFlexTable.setText(i, 1, "security check");
	    	stocksFlexTable.setText(i, 2, "Iverson");
	    	stocksFlexTable.setText(i, 3, "comply");
    	}
    }
	
}

package com.tippingpoint.app.client.scanner;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.tippingpoint.app.client.ScreenInterface;

public class Confirmation implements ScreenInterface {
	private VerticalPanel m_panelMain = new VerticalPanel();
	private FlexTable stocksFlexTable = new FlexTable();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private TextBox newSymbolTextBox = new TextBox();
	private Button addStockButton = new Button("Add");
	private Label lastUpdatedLabel = new Label();
	
	private ArrayList<String> stocks = new ArrayList<String>();

	public Confirmation() {
		// Create table for stock data.
	    stocksFlexTable.setText(0, 0, "Symbol");
	    stocksFlexTable.setText(0, 1, "Price");
	    stocksFlexTable.setText(0, 2, "Change");
	    stocksFlexTable.setText(0, 3, "Remove");
	    // Add styles to elements in the stock list table.
	    stocksFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
	    stocksFlexTable.addStyleName("watchList");
	    stocksFlexTable.getCellFormatter().addStyleName(0, 1, "watchListNumericColumn");
	    stocksFlexTable.getCellFormatter().addStyleName(0, 2, "watchListNumericColumn");
	    stocksFlexTable.getCellFormatter().addStyleName(0, 3, "watchListRemoveColumn");
	    
		// Assemble Add Stock panel.
	    addPanel.add(newSymbolTextBox);
	    newSymbolTextBox.setSize("170px", "38px");
	    addPanel.add(addStockButton);
	    addPanel.addStyleName("addPanel");	    
	    
		// Assemble Main panel.
	    m_panelMain.add(stocksFlexTable);
	    m_panelMain.add(addPanel);
	    m_panelMain.add(lastUpdatedLabel);
	}
	
	public VerticalPanel getMainPanel() { return m_panelMain; }
	
}

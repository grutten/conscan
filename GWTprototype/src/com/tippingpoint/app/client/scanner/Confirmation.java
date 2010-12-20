package com.tippingpoint.app.client.scanner;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.tippingpoint.app.client.ScreenBase;
import com.tippingpoint.app.client.ScreenInterface;

public class Confirmation extends ScreenBase implements ScreenInterface {
	private FlexTable stocksFlexTable = new FlexTable();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private TextBox newSymbolTextBox = new TextBox();
	private Button addStockButton = new Button("Add");
	private Label lastUpdatedLabel = new Label();
	
	private ArrayList<String> stocks = new ArrayList<String>();

	public Confirmation() {
		super();
		
	    class MyPopup extends PopupPanel {

	        public MyPopup() {
	          // PopupPanel's constructor takes 'auto-hide' as its boolean parameter.
	          // If this is set, the panel closes itself automatically when the user
	          // clicks outside of it.
	          super(true);
	          
	          setWidth("600px");
	          setHeight("400px");

	          // PopupPanel is a SimplePanel, so you have to set it's widget property to
	          // whatever you want its contents to be.
	          setWidget(new Label("Click outside of this popup to close it"));
	        }
	      }
	    
	    Button btnOpenLayer = new Button("Click to see a layer");
	    btnOpenLayer.addClickHandler(new ClickHandler() {
	      public void onClick(ClickEvent event) {
	        // Instantiate the popup and show it.
	        final MyPopup popup = new MyPopup();
	        
	        popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
	            public void setPosition(int offsetWidth, int offsetHeight) {
	              int left = (Window.getClientWidth() - offsetWidth) / 3;
	              int top = (Window.getClientHeight() - offsetHeight) / 3;
	              popup.setPopupPosition(left, top);
	            }
	          });
	        
	        popup.show();
	      }
	    });
	    HorizontalPanel panelExtra = new HorizontalPanel();
	    panelExtra.add(btnOpenLayer);
	    m_panelMain.add(panelExtra);
		
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
	
	
}

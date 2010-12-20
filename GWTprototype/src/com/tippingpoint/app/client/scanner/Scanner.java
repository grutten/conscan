package com.tippingpoint.app.client.scanner;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.tippingpoint.app.client.ScreenBase;
import com.tippingpoint.app.client.ScreenInterface;

public class Scanner extends ScreenBase implements ScreenInterface {

	public Scanner() {
		super();
		
	    Button btnOpenConfirmation = new Button("Confirmation");
	    btnOpenConfirmation.addClickHandler(new ClickHandler() {
	      public void onClick(ClickEvent event) {
	        // Instantiate the popup and show it.
	        final ConfirmationPopup popup = new ConfirmationPopup();
	        
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
	    HorizontalPanel panel = new HorizontalPanel();
	    panel.add(btnOpenConfirmation);
	    m_panelMain.add(panel);
		
	}
	
	
}

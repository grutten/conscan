package com.tippingpoint.app.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.tippingpoint.app.client.scanner.Scanner;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TippingPoint implements EntryPoint {
	
	/**
	 * This is the entry point method.
	 */
	@Override
	public void onModuleLoad() {
		RoughCutMenu menu = new RoughCutMenu();
		Scanner confirmation = new Scanner();
	    
	    // Associate the Main panel with the HTML host page.
	    RootPanel.get("divTippingPointMain").add(menu.getMainPanel());
	}
}
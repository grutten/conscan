package com.tippingpoint.handheld.ui;

import java.awt.Frame;

import com.tippingpoint.handheld.data.Data;
import com.tippingpoint.handheld.data.DataInterface;
import com.tippingpoint.handheld.data.Staff;

public class Demo extends Frame {
	static final long serialVersionUID = -1;
	
    /**
     * Sole entry point to the class and application.
     * @param args Array of String arguments.
     */
    /**
     * @param args
     */
    public static void main(String[] args) {
//    	String strConfigFile = "xml\\scanner.xml";
    	String strConfigFile = "./xml/scanner.xml";

    	
   		DataInterface  d = new Data(strConfigFile);
    	System.out.println("scanner.xml - step complete (may have completed with errors)");

    	String strBarcode = "073854016336";
		Staff s = d.getStaffByBarcode(strBarcode);
		System.out.println("staff: " + s.getEmail());

    	
    }

}

package com.tippingpoint.handheld.ui;

import java.awt.Frame;

import com.tippingpoint.handheld.data.Data;
import com.tippingpoint.handheld.data.DataInterface;

public class Application extends Frame {
	static final long serialVersionUID = -1;
	
    /**
     * Sole entry point to the class and application.
     * @param args Array of String arguments.
     */
    /**
     * @param args
     */
    public static void main(String[] args) {
    	boolean bEnvironmentIsHandheld = false;
    	boolean bIsTestEnvOnHandheld = false;
    	String strConfigFile = "xml\\scanner.xml";

    	if (args.length == 2) {   // TEST new scanner.xml
    		strConfigFile = args[0];
    		bEnvironmentIsHandheld = true;
    		bIsTestEnvOnHandheld = true;
    	}
    	else if (args.length > 0) {   // Normal operation
    		strConfigFile = args[0];
    		bEnvironmentIsHandheld = true;
    	}
    	
   		DataInterface  d = null;
    	if (!bEnvironmentIsHandheld) {    	
//        	strConfigFile = "xml\\scanner.xml";
        	strConfigFile = "./xml/scanner.xml";  // MAC
    	    Data d2 = new Data(strConfigFile);
    	    System.out.println(d2.toString());
    	    
        	Screen mainFrame = new Simulator(d2);
        	mainFrame.draw();
        	mainFrame.drawSecurityCheck();
    	} 
    	else if (bIsTestEnvOnHandheld) {
    	    Data d2 = new Data(strConfigFile);
    	    System.out.println(d2.toString());
    	}
    	else {
    		d = new Data(strConfigFile);
    		
        	Screen mainFrame = null;

            if (bEnvironmentIsHandheld)
                mainFrame = new ScreenLayout(d, bEnvironmentIsHandheld);
        	else
                mainFrame = new Simulator(d);
            
            
            mainFrame.draw();
            if (mainFrame instanceof Simulator) {
            	mainFrame.drawSecurityCheck();
//            	mainFrame.drawCellSearch();
            	if (bEnvironmentIsHandheld) System.out.println("");
            }
    		
    	}

    }

}
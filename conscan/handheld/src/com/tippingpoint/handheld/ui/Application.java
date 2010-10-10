package com.tippingpoint.handheld.ui;

import java.awt.Frame;
import com.tippingpoint.handheld.data.Data;

public class Application extends Frame {
	static final long serialVersionUID = -1;
	
    /**
     * Sole entry point to the class and application.
     * @param args Array of String arguments.
     */
    public static void main(String[] args) {
    	boolean bEnvironmentIsHandheld = false;
//    	String strConfigFile = "xml\\handheld.xml";
    	String strConfigFile = "g:\\wkspc\\conscan\\handheld\\xml\\scanner.xml";
    	Data d;

    	if (args.length > 0) {
    		strConfigFile = args[0];
    		bEnvironmentIsHandheld = true;
    	}
    	
    	if (bEnvironmentIsHandheld)
	        d = new Data(strConfigFile);
	    else
    		d = new Data(strConfigFile);
        
    	Screen mainFrame = null;

        if (bEnvironmentIsHandheld)
            mainFrame = new ScreenLayout(d, bEnvironmentIsHandheld);
    	else
            mainFrame = new Simulator(d);
        
        
        mainFrame.draw();
        if (mainFrame instanceof Simulator) {
        	mainFrame.drawSecurityCheck();
//        	mainFrame.drawCellSearch();
        	if (bEnvironmentIsHandheld) System.out.println("");
        }

    }

}

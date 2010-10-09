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
    	String strConfigFile = "xml\\handheld.xml";
    	boolean bEnvironmentIsHandheld = false;
    	Screen mainFrame = null;
    	
    	if (args.length > 0) {
    		strConfigFile = args[0];
    		bEnvironmentIsHandheld = true;
    	}
    	
        Data d = new Data(strConfigFile, Data.CONST_PARSER_CURRENT);
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

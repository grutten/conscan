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

    	if (args.length == 1) {   // Normal operation
    		strConfigFile = args[0];
    		bEnvironmentIsHandheld = true;
    	}
    	
   		DataInterface  d = null;
		d = new Data(strConfigFile);
		
    	Screen mainFrame = null;

        if (bEnvironmentIsHandheld)
            mainFrame = new ScreenLayout(d, bEnvironmentIsHandheld);
        
        
        mainFrame.draw();
    }

}

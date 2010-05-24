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
    	if (args.length > 0) {
    		strConfigFile = args[0];
    		bEnvironmentIsHandheld = true;
    	}
    	
        Data d = new Data(strConfigFile);
    	
        ScreenLayout mainFrame = new ScreenLayout(d, bEnvironmentIsHandheld);
//      Simulator mainFrame = new Simulator();
        mainFrame.draw();

    }

}

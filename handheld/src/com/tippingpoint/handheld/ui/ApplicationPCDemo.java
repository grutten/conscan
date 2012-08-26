package com.tippingpoint.handheld.ui;

import java.awt.Frame;
import java.io.File;

import com.tippingpoint.handheld.data.Data;
import com.tippingpoint.handheld.data.DataInterface;

public class ApplicationPCDemo extends Frame {
	static final long serialVersionUID = -1;
	
    /**
     * Sole entry point to the class and application.
     * @param args Array of String arguments.
     */
    /**
     * @param args
     */
    public static void main(String[] args) {
		String strConfigFile = getConfigFile();
    	
   		DataInterface  d = new Data(strConfigFile);
   		d.parse();

    	Screen mainFrame = new SimulatorScreenLayout(d);
    	
        mainFrame.draw();

    }
    
    private static String getConfigFile() {
    	String strConfigFile = "xml\\scanner.xml";
		File f = new File(strConfigFile);

		if (!f.exists())
			strConfigFile = "xml/scanner.xml";  // mac
		
		f = new File(strConfigFile);
		if (!f.exists()) {
			strConfigFile = "";
			System.out.println("oops, couldn't find scanner.xml for MAC or Windows.");
		}
		
		return strConfigFile;
    }

}

package com.tippingpoint.handheld.ui;

import java.awt.Frame;
import java.io.File;

import com.tippingpoint.handheld.data.Data;
import com.tippingpoint.handheld.data.DataInterface;
import com.tippingpoint.handheld.data.DataRegression;

public class ApplicationRegression extends Frame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5213130630579650012L;

	public static void main(String[] args) {
		System.out.println("Begin Unit Test");

		String strConfigFile = getFileString();

		// These is the real object to verify
   		DataInterface  dataProductionObj = null;
   		dataProductionObj = new Data(strConfigFile);
   		dataProductionObj.parse();
		
		DataRegression d = new DataRegression(strConfigFile);
		d.verify(dataProductionObj);
	}
	
	private static String getFileString() {
		String strConfigFile = "xml/scanner.xml";  // mac
		
		File f = new File(strConfigFile);
		if (!f.exists())
			strConfigFile = "\\My Documents\\scanner.xml";  // windows
		
		f = new File(strConfigFile);
		if (!f.exists())
			strConfigFile = "";
		
		return strConfigFile;
	}
}

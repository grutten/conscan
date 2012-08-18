package com.tippingpoint.handheld.ui;

import java.awt.Frame;

import com.tippingpoint.handheld.data.Data;
import com.tippingpoint.handheld.data.DataInterface;
import com.tippingpoint.handheld.data.DataRegression;

public class ApplicationRegression extends Frame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5213130630579650012L;

	public static void main(String[] args) {
		String strConfigFile = "xml/scanner.xml";

		// These is the real object to verify
   		DataInterface  dataProductionObj = null;
   		dataProductionObj = new Data(strConfigFile);
   		dataProductionObj.parse();
		
		DataRegression d = new DataRegression(strConfigFile);
		d.parse();
		d.verify(dataProductionObj);
	}
}

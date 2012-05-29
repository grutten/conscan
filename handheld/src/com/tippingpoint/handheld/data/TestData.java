package com.tippingpoint.handheld.data;

import junit.framework.TestCase;

public class TestData extends TestCase {
	
	// BUG: Cell with 2 offenders.  The scanner log should result with 2 offenders
	// where the location is the same.  The bug that is occurring is that each
	// scannerlog record contains a different location.
	// NOTE: upon initial check-in, this file doesn't actually prove anything yet.
	//       The originalbug reported above seemed to be alleviated by updating
	// 			the orginal 3 offenders' booking numbers with the format P-99999.
	//			However, after spending a couple of hours in the code, it is not
	//			apparent why the P-99999 data change alleviated the problem since
	// 			the data that showed the bug should have had the correct values
	// 			in the offederlocation table; that is, the offenderlocation table
	// 			had been loaded prior to the booking number format change.
	public void testOffendersLoc4TwoOffendersIn1Cell() {
//    	String strConfigFile = "xml\\scanner.xml";	// WIN
    	String strConfigFile = "./xml/scanner.xml";  // MAC
	    Data data = new Data(strConfigFile);

		Activity a = Util.findActivity(data, "security check"); 
		data.populateScannables("0070718001170", a);
	    Util.addLogEntry(data, "security check");
	    
	    System.out.println("The END");
	}
}

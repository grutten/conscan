package com.tippingpoint.handheld.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.time.DateFormatUtils;

public class Util {
	public static LogEntry addLogEntry(DataInterface data, String strActivity) {
		ArrayList arrScannables = data.getScannables();

		// persist
		Activity a = findActivity(data, strActivity); 
        Iterator i = arrScannables.iterator();
        LogEntry logEntry = data.getLogEntry();
        logEntry.setActivity(a);
        
		try {  // wraps the write of the log entry
	        while (i.hasNext()) {
	            Date d = new Date();
	            logEntry.setDateCreated(DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(d));
	            
	        	// TODO: this may need to persist 2 offenders for the detail screen.
	        	// What does that mean for the detail screen?  Does it matter what
	        	// order offenders are persisted when there are 2 or more offenders
	        	// in one cell?  Probably not, but is something to think about.
	        	
	        	// Write: a) offender b) cell + offender c) cell
	        	Object obj = i.next();
	        	Scannable scannable = null;
	        	
	        	if (obj instanceof Scannable)
	        		scannable = (Scannable) obj;
	        	
	        	// TODO: handle a null scannable.  Figure out a graceful
	        	// way to display a useable state on the handheld.  Is
	        	// a null scannable something that is expected to happen?
	        	// Or is it realy a bug and should never happen?
	        	
	    		Object objCompliance = scannable.getComplianceControl().getSelectedItemObject();
	    		
	    		if (objCompliance instanceof ComplianceValue) {
	    			ComplianceValue cv = (ComplianceValue)objCompliance;
	    			logEntry.setComplianceValue(cv);
	    		}
	    		
	        	if (scannable.getObject() instanceof Offender) {
	        		Offender offender = (Offender)scannable.getObject();
	        		logEntry.setOffender(offender);
	        		
	        		Location location = data.getLocationByOffender(offender);
	        		logEntry.setLocation(location);
	        		}
	        	else if (scannable.getObject() instanceof Location) {
	        		Location location = (Location)scannable.getObject();
	    			logEntry.setLocation(location);
	
	        		if (a.isOffenderCompliance()) {
	        	        System.out.println("addLogEntry(): UNEXPECTED CONDITION");
	        		}
	        	}
	        	else
	    	        System.out.println("scannable object is not an offender or a location ");
	        		
	        	logEntry.setScannedBarcode(data.getBarcode());	        	
				logEntry.write();
	        }
		}
		catch (Exception e) {
			// This is typically thrown when the location of the log file couldn't
			// be found
			System.out.println(e.toString());
			e.printStackTrace();
		}
		
		return logEntry;
	}
	
    public static Activity findActivity(DataInterface d, String strActivity) {
    	Iterator iActivities = d.getActivities().iterator();
    	Activity searchResult = null;
    	while (searchResult == null && iActivities.hasNext()) {
    		Activity currentItem = (Activity)iActivities.next();
    		if (currentItem != null)
    			if(currentItem.getName().equalsIgnoreCase(strActivity))
    				searchResult = currentItem;
    	}
    	
    	return searchResult;
    }

}

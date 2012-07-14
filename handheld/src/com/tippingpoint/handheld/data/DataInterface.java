package com.tippingpoint.handheld.data;

import java.util.ArrayList;
import java.util.HashMap;

public interface DataInterface {
	public ArrayList getActivities();

	public ArrayList addList(String strKeyName, ArrayList arrList);
	public void addObject(String strName, Object obj);
	public void clear();
	public ArrayList createList(String strKeyName);
	public String getBarcode();
	public HashMap getCompliance();
	public Object getCurrentObject();
	public String getFeedback();
	public Location getLocationByOffender(Offender offender);
	public HashMap getLocations();
	public LogEntry getLogEntry();
	public Staff getLoggedInStaff();
	public HashMap getOffenders();
	public ArrayList getScannables();
	public Staff getStaffByBarcode(String strBarcode);
	public void parse();
	public Object popObject();
	public ArrayList populateScannables(String strBarcode, Activity activity);
	public void reset();
	
//	public void saveActivity(Activity a);
//	public void saveCompliance(ComplianceConfiguration compliance);
//	public void saveLocation(Location location);
//	public void saveOffender(Offender offender);
	public void setBarcode(String strBarcode);
	public void clearLoggedInStaff();
	public void setLoggedIntStaff(Staff staff);
//	public void setFeedback(String strFeedback);
	
}

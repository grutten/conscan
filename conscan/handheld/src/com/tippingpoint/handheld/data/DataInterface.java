package com.tippingpoint.handheld.data;

import java.util.ArrayList;

public interface DataInterface {
	public ArrayList getActivities();

	public void saveActivity(Activity a);
	public void saveCompliance(ComplianceConfiguration compliance);
	public void saveLocation(Location location);
	public void saveOffender(Offender offender);
	public void setBarcode(String strBarcode);
//	public void setFeedback(String strFeedback);
	
}

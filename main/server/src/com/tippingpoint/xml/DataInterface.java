package com.tippingpoint.xml;


import java.util.ArrayList;

import com.tippingpoint.conscan.objects.BusinessObject;

public interface DataInterface {

	public String getObjectName();
	public void add(BusinessObject object);
	public ArrayList<BusinessObject> get();
	public void setObjectName(String strName);

	
}

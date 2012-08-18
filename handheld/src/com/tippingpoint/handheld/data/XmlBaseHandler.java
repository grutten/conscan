package com.tippingpoint.handheld.data;

import java.lang.reflect.Method;

import org.xml.sax.XMLReader;

import com.tippingpoint.util.xml.SaxBaseHandler;

public class XmlBaseHandler extends SaxBaseHandler {
	protected static final String ATTR_NAME = "name";
	
	protected static final String CLASS_LOCATION = "Location";
	protected static final String CLASS_OFFENDER = "Offender";
	
	protected static final String OBJ_ACTIVITY = "activity";
	protected static final String OBJ_COMPLIANCE = "compliance";
	protected static final String OBJ_COMPLIANCEVALUE = "compliancevalue";
	protected static final String OBJ_LOCATION = "location";
	protected static final String OBJ_OFFENDER = "offender";
	protected static final String OBJ_STAFF = "staff";
	
	protected static final String TAG_CONFIGURATION = "configuration";
	protected static final String TAG_FIELD = "field";
	protected static final String TAG_LIST = "list";
	protected static final String TAG_OBJECT = "object";

	protected String m_strCurrentFieldName;
	
	public XmlBaseHandler(SaxBaseHandler parentHandler, XMLReader reader) {
		super(parentHandler, reader);
	}

	protected void logEndElement(String strTagName, String uri, String name, String qName) {
		if ("".equals (uri))
			handheldLog("End element (" + strTagName + "): " + (qName != null ? qName : ""));
		else
			handheldLog("End element (" + strTagName + "): {" + uri + "}" + name);
	}
    
	protected void logStartElement(String strTagName, String uri, String name, String qName) {
		if ("".equals (uri))
			handheldLog("Start element (" + strTagName + "): " + (qName != null ? qName : ""));
		else
			handheldLog("Start element (" + strTagName + "): {" + uri + "}" + name);
	}
	
	protected void setField(String strFieldName, String strValue, Object o) {
		Class cls = o.getClass();
		Method[] methods = cls.getDeclaredMethods();
		Method methodToInvoke = null;
		String strMethodName = "set" + strFieldName;
		
		// Find the method for the field being set
		int nMethodCount = methods.length;
		for (int i = 0; i < nMethodCount; ++i) {
			Method m = methods[i];
			
			if (strMethodName.equalsIgnoreCase(m.getName()))
				methodToInvoke = m;
			if (methodToInvoke != null)
				break;
		}
		
		// Set the field's value if the method was found
   		if (methodToInvoke != null) {
			try {
				methodToInvoke.invoke(o, new Object[] {strValue});
			}
			catch (Exception e) {
				System.out.println(e.getStackTrace());
			}
		}
	}
	
}

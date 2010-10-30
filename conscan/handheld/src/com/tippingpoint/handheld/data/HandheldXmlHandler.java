package com.tippingpoint.handheld.data;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import com.tippingpoint.util.xml.SaxBaseHandler;

/**
 * Parsing algorithm:
 * <configuration> -> create root map
 * 		<list> - if no current object, create map entry in root's map for list (e.g. locations or compliances)
 * 			   - else, create map entry in current object's map for list (e.g. compliancevalues)
 * 		<object> - add to current map
 * 		<field> - save to current object
 * @author mgee
 *
 */
public class HandheldXmlHandler extends SaxBaseHandler {
	protected static final String ATTR_NAME = "name";
	
	protected static final String CLASS_LOCATION = "Location";
	protected static final String CLASS_OFFENDER = "Offender";
	
	protected static final String OBJ_LOCATION = "location";
	protected static final String OBJ_OFFENDER = "offender";
	
	protected static final String TAG_CONFIGURATION = "configuration";
	protected static final String TAG_FIELD = "field";
	protected static final String TAG_LIST = "list";
	protected static final String TAG_OBJECT = "object";
	
	private Data m_data;
	private String m_strCurrentFieldName;
	
	HandheldXmlHandler(SaxBaseHandler parentHandler, XMLReader reader, Data d) {
		super(parentHandler, reader);
		
		m_data = d;
	}

	public void endElement (String uri, String name, String qName) {
		// always pop the current object unless it's a field!!!
    	if (TAG_FIELD.equalsIgnoreCase(qName)) {
    		setField(m_strCurrentFieldName, m_strCurrentTagValue);
    		logStartElement(qName, uri, name, "field: " + m_strCurrentFieldName + " - " + m_strCurrentTagValue);
    	}
    	else if (!TAG_FIELD.equalsIgnoreCase(qName)) {
			Object o = getData().popObject();
			if (o != null)
				System.out.println("end Element - pop obj type: " + o.getClass().getName());
		}
	}
	
    public void startElement (String uri, String name, String qName, Attributes attrs) {
    	String strCurrObjName = attrs.getValue("name");
    	String strCurrObjAttr = attrs.getValue("name") + "id";
    	String strCurrObjValue = attrs.getValue(strCurrObjAttr);
    	String strCurrObjNameValuePair = strCurrObjAttr + "=" + strCurrObjValue;
    	m_strCurrentFieldName = null;
    	
    	if (TAG_LIST.equalsIgnoreCase(qName)) {
    		// Add a list.  Note, the list may be coming from a business object
    		// such as Location, Compliance instead of creating a new List to
    		// hold a root level entity such as Activities.
    		addList(attrs.getValue(ATTR_NAME));
    		logStartElement(qName, uri, name, "name=" + strCurrObjName);
    	}
    	else if (TAG_OBJECT.equalsIgnoreCase(qName)) {
    		Object currObj = createObject(strCurrObjName);
    		getData().addObject(strCurrObjName, currObj);
    		
    		logStartElement(qName, uri, name, strCurrObjNameValuePair);
    	}
    	else if (TAG_FIELD.equalsIgnoreCase(qName)) {
    		m_strCurrentFieldName = strCurrObjName;
    	}
    	else if (!"field".equalsIgnoreCase(qName))
    		logStartElement(qName, uri, name, strCurrObjName);
//    	else
//    		logStartElement("HH", uri, name, qName);
    }
	
    
    
    private ArrayList addList(String strName) {
    	Object o = getData().getCurrentObject();
    	ArrayList arrList;
    	
    	if (o instanceof Location) {
    		Location l = (Location)o;
    		arrList = l.getOffenders();
    		getData().addList(strName, arrList);
    	}
    	else if (o instanceof ComplianceConfiguration) {
    		ComplianceConfiguration c = (ComplianceConfiguration)o;
    		arrList = c.getValues();
    		getData().addList(strName, arrList);
    	}
    	else
    		arrList = getData().createList(strName);
    
    	return arrList;
    }
    
    private Data getData() { return m_data; }

    private Object createObject(String strObjectName) {
    	Object object = null;
    	
    	if (OBJ_LOCATION.equalsIgnoreCase(strObjectName))
    		object = new Location();
    	else if (OBJ_OFFENDER.equalsIgnoreCase(strObjectName))
    		object = new Offender();
    	return object;
    }
    
	private void logEndElement(String strTagName, String uri, String name, String qName) {
		if ("".equals (uri))
			handheldLog("End element (" + strTagName + "): " + (qName != null ? qName : ""));
		else
			handheldLog("End element (" + strTagName + "): {" + uri + "}" + name);
		
	}
    
	private void logStartElement(String strTagName, String uri, String name, String qName) {
		if ("".equals (uri))
			handheldLog("Start element (" + strTagName + "): " + (qName != null ? qName : ""));
		else
			handheldLog("Start element (" + strTagName + "): {" + uri + "}" + name);
	}
	
	private void setField(String strFieldName, String strValue) {
    	Object o = getData().getCurrentObject();
//    	String strClassPath = "com.tippingpoint.handheld.data.";
    	
//    	if ((strClassPath + CLASS_LOCATION).equalsIgnoreCase(o.getClass().getName())) {
//    		Location l = (Location)o;
    		Class cls = o.getClass();
    		Method[] methods = cls.getDeclaredMethods();
    		Method methodToInvoke = null;
    		String strMethodName = "set" + strFieldName;
    		
    		for (Method m: methods) {
    			if (strMethodName.equalsIgnoreCase(m.getName()))
    				methodToInvoke = m;
    			if (methodToInvoke != null)
    				break;
    		}
//    		if (methodToInvoke != null  & l != null) {
       		if (methodToInvoke != null) {
    			try {
    				methodToInvoke.invoke(o, new Object[] {strValue});
    			}
    			catch (Exception e) {
    				System.out.println(e.getStackTrace());
    			}
    		}
//    	}
    	
	}
    
}

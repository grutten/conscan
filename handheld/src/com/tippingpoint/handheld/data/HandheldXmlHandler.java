package com.tippingpoint.handheld.data;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import com.tippingpoint.util.string.StringFormat;
import com.tippingpoint.util.xml.SaxBaseHandler;

/**
 * Parsing algorithm:
 * <configuration> -> create root map
 * 		<list> - if no current object, create map entry in root's map for list 
 * 					(e.g. locations, compliances, or activities)
 * 			   - else, create map entry in current object's map for list 
 * 					(e.g. offenders, compliancevalues, {no child objects for activity})
 * 		<object> - add to current map object, an instance representing one of the <list>
 * 					entries found in the XML.
 * 		<field> - save to current map object, an instance representing one of the <object>
 * 					entries found in the XML.
 * @author mgee
 *
 */
public class HandheldXmlHandler extends XmlBaseHandler {
	protected DataInterface m_data;
	
	HandheldXmlHandler(SaxBaseHandler parentHandler, XMLReader reader, DataInterface d) {
		super(parentHandler, reader);
		
		m_data = d;
	}

	public void endElement (String uri, String name, String qName) {
		// always pop the current object unless it's a field!!!
    	if (TAG_FIELD.equalsIgnoreCase(qName)) {
    		setField(m_strCurrentFieldName, getCurrentTagValue(), getData().getCurrentObject());
    		logEndElement(qName, uri, name, "field: " + m_strCurrentFieldName + " - " + getCurrentTagValue());
    	}
    	else if (!TAG_FIELD.equalsIgnoreCase(qName)) {
			Object o = getData().popObject();
			if (o != null)
				handheldLog("end Element - pop obj type: " + o.getClass().getName());
		}
	}
	
    public void startElement (String uri, String name, String qName, Attributes attrs) {
    	super.startElement(uri, name, qName, attrs);
    	
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
    		// Create the object and store it
    		Object currObj = createObject(strCurrObjName);
    		getData().addObject(strCurrObjName, currObj);
    		
    		// If the current object has an ID, set it
    		String strObjectIdAttributeName = strCurrObjName + "id";
    		String strObjectIdValue = attrs.getValue(strObjectIdAttributeName);
    		if (StringFormat.isSpecified(strObjectIdValue))
        		setField(strObjectIdAttributeName, strObjectIdValue, getData().getCurrentObject());
    		
    		logStartElement(qName, uri, name, strCurrObjNameValuePair);
    	}
    	else if (TAG_FIELD.equalsIgnoreCase(qName)) {
    		m_strCurrentFieldName = strCurrObjName;
    	}
    	else if (!"field".equalsIgnoreCase(qName))
    		logStartElement(qName, uri, name, strCurrObjName);
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
    
    private Object createObject(String strObjectName) {
    	Object object = null;
    	
    	if (OBJ_ACTIVITY.equalsIgnoreCase(strObjectName))
    		object = new Activity();
    	else if (OBJ_LOCATION.equalsIgnoreCase(strObjectName))
    		object = new Location();
    	else if (OBJ_OFFENDER.equalsIgnoreCase(strObjectName))
    		object = new Offender();
    	else if (OBJ_COMPLIANCE.equalsIgnoreCase(strObjectName))
    		object = new ComplianceConfiguration();
    	else if (OBJ_COMPLIANCEVALUE.equalsIgnoreCase(strObjectName))
    		object = new ComplianceValue();
    	else if (OBJ_STAFF.equalsIgnoreCase(strObjectName))
    		object = new Staff();
    	
    	return object;
    }

    private DataInterface getData() { return m_data; }
}

package com.tippingpoint.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.conscan.servlet.Activity;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.xml.DataInterface;
import com.tippingpoint.xml.SaxBaseHandler;

public class GenericXmlHandler extends SaxBaseHandler {

	protected static final String TAG_FIELD = "field";
	protected static final String TAG_OBJECT = "object";
	
	private DataInterface m_data;
	private static Log m_log = LogFactory.getLog(Activity.class);
	private BusinessObject m_objCurrentObject;
	private String m_strCurrentFieldName;
	
	public GenericXmlHandler(SaxBaseHandler parentHandler, XMLReader reader, DataInterface d) {
		super(parentHandler, reader);
		
		m_data = d;
//		m_log.
	}
	
	@Override
	public void endElement(String uri, String name, String qName) {
    	if (TAG_FIELD.equals(qName)) {
    		m_log.debug("Field : " + m_strCurrentFieldName + " - " + m_strCurrentTagValue);
    		if (m_objCurrentObject != null) {
				m_objCurrentObject.setValue(m_strCurrentFieldName, m_strCurrentTagValue);
				m_strCurrentFieldName = null;
    		}
    		else
        		m_log.debug("Unexpected null object in GenericXmlHandler");

    	}
    	else if (TAG_OBJECT.equals(qName)) {
    		if (m_objCurrentObject != null) {
///*    			
    			try {
    				m_objCurrentObject.save();
    			}
    			catch (SqlBaseException e) {
    				m_log.debug("Unexpected error saving Business Object: " + e.toString());
    			}
//*/    			
    		}
    		else
        		m_log.debug("Unexpected null object in GenericXmlHandler");
    	}
	}
	
	@Override
    public void startElement (String uri, String name, String qName, Attributes attrs) {
    	String strCurrObjName = attrs.getValue("name");
    	
    	if (TAG_OBJECT.equals(qName))  {
    		final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(strCurrObjName);
    		
    		if (builder == null) {
    			m_log.debug("NOT FOUND the following object specified in XML could not be found in the schema: " + strCurrObjName);
    		}
    		else {
    			m_objCurrentObject = builder.get();
    			if (m_objCurrentObject == null)
    				m_log.debug("GenericXmlHandler: unexpected object creation failure - " + strCurrObjName);
    			else {
    				m_data.setObjectName(strCurrObjName);
    				m_log.debug("GenericXmlHandler: successful object creation - " + strCurrObjName);
    			}
    		}
    	}
    	else if (TAG_FIELD.equals(qName)) {
    		m_strCurrentFieldName = strCurrObjName;
    	}
    	
    }
}

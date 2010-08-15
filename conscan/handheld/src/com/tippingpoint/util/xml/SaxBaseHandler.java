package com.tippingpoint.util.xml;

import java.io.CharArrayWriter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class SaxBaseHandler extends DefaultHandler {
	private SaxBaseHandler m_ParentHandler;
	private XMLReader m_xmlReader;
	
	protected static final String ATTRIBUTE_ACTIVITYID = "activityid";
	protected static final String ATTRIBUTE_COMPLIANCECONFIGURATIONID = "complianceconfigurationid";
	protected static final String ATTRIBUTE_COMPLIANCEVALUEID = "compliancevalueid";
	protected static final String ATTRIBUTE_LOCATIONID = "locationid";
	protected static final String ATTRIBUTE_OFFENDERID = "offenderid";
	
	protected static final String TAG_ACTIVITIES = "activities";
	protected static final String TAG_ACTIVITY = "activity";
	protected static final String TAG_BARCODE = "barcode";
	protected static final String TAG_BOOKINGNUMBER = "bookingnumber";
	protected static final String TAG_COMPLIANCECONFIGURATION = "complianceconfiguration";
	protected static final String TAG_COMPLIANCECONFIGURATIONS = "complianceconfigurations";
	protected static final String TAG_COMPLIANCETYPE = "compliancetype";
	protected static final String TAG_COMPLIANCEVALUE = "compliancevalue";
	protected static final String TAG_COMPLIANCEVALUES = "compliancevalues";
	protected static final String TAG_LOCATION = "location";
	protected static final String TAG_LOCATIONS = "locations";
	protected static final String TAG_NAME = "name";
	protected static final String TAG_OFFENDER = "offender";
	protected static final String TAG_OFFENDERS = "offenders";
	protected static final String TAG_SCANTYPE = "scantype";
	protected static final String TAG_TIER = "tier";
	protected static final String TAG_VALUE = "value";
	
	protected String m_strCurrentTagValue;
	
	public SaxBaseHandler(SaxBaseHandler parentHandler, XMLReader reader) {
		super();

		m_ParentHandler = parentHandler;
		m_xmlReader = reader;
	}
	
    public void characters (char ch[], int start, int length) {
    	CharArrayWriter writer = new CharArrayWriter();
    	writer.write(ch, start, length);
    	m_strCurrentTagValue = writer.toString();
    }	
    
	public SaxBaseHandler getParentHandler() { return m_ParentHandler; }

	public XMLReader getReader() { return m_xmlReader; }
	
	public void popHandler() {
		m_xmlReader.setContentHandler(getParentHandler());
	}
	
	public void pushHandler(SaxBaseHandler newHandler) {
		m_xmlReader.setContentHandler(newHandler);
	}
	
}

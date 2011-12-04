package com.tippingpoint.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class SaxBaseHandler extends DefaultHandler {
	
	private boolean m_bLoggingEnabled = false;
	private SaxBaseHandler m_ParentHandler;
	private XMLReader m_xmlReader;

	protected static final String VALUE_DEFAULT_TRUE = "true";
	protected static final String VALUE_DEFAULT_FALSE = "false";
	
	private StringBuffer m_sbBody = new StringBuffer();
	
	public SaxBaseHandler(SaxBaseHandler parentHandler, XMLReader reader) {
		super();

		m_ParentHandler = parentHandler;
		m_xmlReader = reader;
	}
	
    public void characters (char ch[], int start, int length) {
    	m_sbBody.append(ch, start, length);
    }	
    
    public String getCurrentTagValue() {
    	return m_sbBody.toString();
    }
    
	public SaxBaseHandler getParentHandler() { return m_ParentHandler; }

	public XMLReader getReader() { return m_xmlReader; }
	
    public void endDocument () {
    	handheldLog("End document");
    }
	
	public void popHandler() {
		m_xmlReader.setContentHandler(getParentHandler());
	}
	
	public void pushHandler(SaxBaseHandler newHandler) {
		m_xmlReader.setContentHandler(newHandler);
	}
	
    public void startDocument () {
    	handheldLog("Start document");
    }
	
    public void startElement (String uri, String name, String qName, Attributes attrs) {
    	m_sbBody.delete(0, m_sbBody.length());
    }
    
	protected void handheldLog(String str) { if (m_bLoggingEnabled) System.out.println(str); }
	
	
}

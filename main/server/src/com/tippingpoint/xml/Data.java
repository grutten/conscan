package com.tippingpoint.xml;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.xml.GenericXmlHandler;
import com.tippingpoint.xml.SaxBaseHandler;

public class Data implements DataInterface {

	private String m_strObjectName = "UNSET";
	private XMLReader m_xmlreader;
	private ArrayList<BusinessObject> m_listObjects = new ArrayList<BusinessObject>();
	
	public Data(String strFilename) {
		try {
			m_xmlreader = XMLReaderFactory.createXMLReader();
			SaxBaseHandler saxHandler = new GenericXmlHandler(null, m_xmlreader, this);
			
			m_xmlreader.setContentHandler(saxHandler);
			m_xmlreader.setErrorHandler(saxHandler);
			FileReader reader = new FileReader(strFilename);
			m_xmlreader.parse(new InputSource(reader));
		}
		catch (Exception e) {
			System.out.println("Failed to create XMLReader" + e.toString());
		}
		System.out.println("done parsing XML.");
	}
	
	public Data(Reader reader) {
		try {
			m_xmlreader = XMLReaderFactory.createXMLReader();
			SaxBaseHandler saxHandler = new GenericXmlHandler(null, m_xmlreader, this);
			
			m_xmlreader.setContentHandler(saxHandler);
			m_xmlreader.setErrorHandler(saxHandler);
			
			m_xmlreader.parse(new InputSource(reader));
		}
		catch (Exception e) {
			System.out.println("Failed to create XMLReader" + e.toString());
		}
		System.out.println("done parsing XML.");
	}

	public void add(BusinessObject object) {
		m_listObjects.add(object);
	}
	
	public ArrayList<BusinessObject> get() {
		return m_listObjects;
	}
	
	@Override
	public String getObjectName() {
		return m_strObjectName;
	}
	
	
	public void setObjectName(String strName) {
		m_strObjectName = strName;
	}
}

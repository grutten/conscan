package com.tippingpoint.handheld.data;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import com.tippingpoint.util.xml.SaxBaseHandler;

public class DataRegression extends Data {
	public DataRegression(String strFilename) {
		super(strFilename);
	}
	
	public boolean verify(DataInterface dataProdcutionObj) {
		try {
			
			
			m_xmlreader = XMLReaderFactory.createXMLReader();
			SaxBaseHandler saxHandler = new TestHarnessXmlHandler(null, m_xmlreader, dataProdcutionObj);
			
			m_xmlreader.setContentHandler(saxHandler);
			m_xmlreader.setErrorHandler(saxHandler);
			FileReader reader = new FileReader(m_strScannerXmlPath);
			// TODO: there is a null pointer exception during startup if the
			// path to the XML config is incorrect in the .LNK shortcut file.
			// The 'reader' is not the culprit since this println is not in the output.
			if (reader == null)
				System.out.println("XML configuration file missing.");
				
			m_xmlreader.parse(new InputSource(reader));
			reader.close();
		}
		catch (SAXException e){
			
		}
		catch (FileNotFoundException e){
		
		}
		catch (IOException e){
		
		}
		
		return true;
	}
}

package com.tippingpoint.handheld.data;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.tippingpoint.util.xml.SaxBaseHandler;

public class Data implements DataInterface{
	XMLReader m_xmlreader;

	// storage
	private HashMap m_hashRoot = new HashMap();
	private Stack m_stackCurrObj = new Stack();
	
	public Data(String strFilename) {
		try {
			// The current object is the root hash map
			m_stackCurrObj.push(m_hashRoot);
			
			m_xmlreader = XMLReaderFactory.createXMLReader();
			SaxBaseHandler saxHandler = new HandheldXmlHandler(null, m_xmlreader, this);
			
			m_xmlreader.setContentHandler(saxHandler);
			m_xmlreader.setErrorHandler(saxHandler);
			FileReader reader = new FileReader(strFilename);
			// TODO: there is a null pointer exception during startup if the
			// path to the XML config is incorrect in the .LNK shortcut file.
			// The 'reader' is not the culprit since this println is not in the output.
			if (reader == null)
				System.out.println("XML configuration file missing.");
				
			m_xmlreader.parse(new InputSource(reader));
//			populateLocationsByOffendersBarcode(m_hashLocationByOffendersBarcode, m_hashLocationByBarcode);
		}
		catch (Exception e) {
			System.out.println("Failed to create XMLReader");
		}
		System.out.println("done loading configuration.");
	}
	
	/**
	 * This method does not add a list at the root level.  It simply makes the
	 * passed in list the current object on the top of the stack.  This is
	 * used when the list is a data member of a business object.
	 * @param strKeyName
	 * @param arrList
	 * @return
	 */
	public ArrayList addList(String strKeyName, ArrayList arrList) {
		m_stackCurrObj.push(arrList);
		
		return arrList;
		
	}
	
	/**
	 * This method creates a new list and adds it to the root map.
	 * @param strKeyName
	 * @return
	 */
	public ArrayList createList(String strKeyName) {
		ArrayList arrList = new ArrayList();
		
		m_hashRoot.put(strKeyName, arrList);
		m_stackCurrObj.push(arrList);
		
		return arrList;
	}

	public void addObject(String strName, Object obj) {
		Object o = m_stackCurrObj.peek();
		ArrayList list = (ArrayList)o;
		if (list != null) {
			list.add(obj);
			m_stackCurrObj.push(obj);
		}
		else {
			// TODO: throw exception????
			System.out.println("Unexpected object type on top of stack: " );
		}
	}
	
	public Object popObject() {
		return m_stackCurrObj.pop();
	}
	
	public Object getCurrentObject() {
		return m_stackCurrObj.peek();
	}
	
	// TODO: Have a validation method that runs through each collection
	// (e.g. list or map) contained by the root map.  The validation method
	// should check that each object it contains is of the specified 
	// type.  For example, the activity list should only contain activity
	// objects.  This validation method should log an entry for each
	// invalid object and the type of that object for debugging purposes.
	// It would also be useful if each invalid object could have its key logged.
	
	public ArrayList getActivities() { return null; }
	public void saveActivity(Activity a) { ; /* asdf */ }
	public void saveCompliance(ComplianceConfiguration compliance){ ; /* asdf */ }
	public void saveLocation(Location location){ ; /* asdf */ }
	public void saveOffender(Offender offender){ ; /* asdf */ }
	public void setBarcode(String strBarcode){ ; /* asdf */ }
	
}

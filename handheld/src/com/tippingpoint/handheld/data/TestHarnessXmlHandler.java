package com.tippingpoint.handheld.data;

import java.lang.reflect.Method;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import com.tippingpoint.util.string.StringFormat;
import com.tippingpoint.util.xml.SaxBaseHandler;

/**
 * The purpose of this class is to verify that each of the objects represented
 * in the scanner's XML load file can be found successfully in the collections
 * stored in the Handheld's hashmaps.  Furthermore, each of the fields in each
 * objects is verified.
 */
public class TestHarnessXmlHandler extends XmlBaseHandler {
	private Stack m_stackCurrObj = new Stack();

	private DataInterface m_dataProduction;
	private String m_strCurrentFieldName;

	
	TestHarnessXmlHandler(SaxBaseHandler parentHandler, XMLReader reader, DataInterface dataProductionObj) {
		super(parentHandler, reader);
		
		m_dataProduction = dataProductionObj;
	}
	
	
	public void endElement (String uri, String name, String qName) {
		Object objCurrent = null;
		if (!m_stackCurrObj.empty())
			objCurrent = m_stackCurrObj.peek();
		
		// always pop the current object unless it's a field!!!
    	if (TAG_FIELD.equalsIgnoreCase(qName)) {
    		setField(m_strCurrentFieldName, getCurrentTagValue(), objCurrent);
    		logEndElement(qName, uri, name, "field: " + m_strCurrentFieldName + " - " + getCurrentTagValue());
    	}
    	else if (TAG_OBJECT.equalsIgnoreCase(qName)) {
    		// TODO: test the completed object here against the one found in the map
    		verify(objCurrent, m_dataProduction);
    		if (objCurrent == null)
    			System.out.println("FOUND");
    		
    		// TODO: set the curr obj to Null
    		if (!m_stackCurrObj.empty()) {
    			Object o = m_stackCurrObj.pop();
    			clearAllFields(o);
    		}
//			Object o = getData().popObject();
//			if (o != null)
//				handheldLog("end Element - pop obj type: " + o.getClass().getName());
		}
	}
	
    public void startElement (String uri, String name, String qName, Attributes attrs) {
    	super.startElement(uri, name, qName, attrs);  // required for lexical parsing success
    	
    	String strCurrObjName = attrs.getValue("name");
    	String strCurrObjAttr = attrs.getValue("name") + "id";
    	String strCurrObjValue = attrs.getValue(strCurrObjAttr);
    	String strCurrObjNameValuePair = strCurrObjAttr + "=" + strCurrObjValue;
    	m_strCurrentFieldName = null;
    	
    	if (TAG_LIST.equalsIgnoreCase(qName)) {
    		// the testing framework doesn't need to do anything
    	}
    	else if (TAG_OBJECT.equalsIgnoreCase(qName)) {
    		// Create the object and store it
    		Object objCurrent = getObject(strCurrObjName);
    		m_stackCurrObj.push(objCurrent);
    		
    		// If the current object has an ID, set it
    		String strObjectIdAttributeName = strCurrObjName + "id";
    		String strObjectIdValue = attrs.getValue(strObjectIdAttributeName);
    		if (StringFormat.isSpecified(strObjectIdValue))
        		setField(strObjectIdAttributeName, strObjectIdValue, objCurrent);
    		
    		logStartElement(qName, uri, name, strCurrObjNameValuePair);
    	}
    	else if (TAG_FIELD.equalsIgnoreCase(qName)) {
    		m_strCurrentFieldName = strCurrObjName;
    	}
    	else if (!"field".equalsIgnoreCase(qName))
    		logStartElement(qName, uri, name, strCurrObjName);
    }

    private static final Object[] objEmptyString = new Object[]{""};
	protected void clearAllFields(Object o) {
		Class cls = o.getClass();
		Method[] methods = cls.getDeclaredMethods();
		Method methodToInvoke = null;
		
		// Find the method for the field being set
		int nMethodCount = methods.length;
		for (int i = 0; i < nMethodCount; ++i) {
			Method m = methods[i];
			
			if (m.getName().startsWith("set")) {
				methodToInvoke = m;
				try {
					methodToInvoke.invoke(o, objEmptyString);
				}
				catch (Exception e) {
//					System.out.println(e.getStackTrace());
					// TODO: how to avoid Location.LIST, which is causing the exception
				}
			}
		}
		
	}
    
    
    // With the initial structure of the XML file and its objects, objects of the
    // same type are never nested; that is, you could never find an offender in the
    // this class's parsing stacktwice. However, if the relationship ever creates
    // this nested relationship,then these single static objects will no longer work.
    // This approach has been taken to speed things up.
    private static final Activity m_objActivity = new Activity();
    private static final Location m_objLocation = new Location();
    private static final Offender m_objOffender = new Offender();
    private static final ComplianceConfiguration m_objCompliance = new ComplianceConfiguration();
    private static final ComplianceValue m_objComplianceValue = new ComplianceValue();
    private static final Staff m_objStaff = new Staff();
    private Object getObject(String strObjectName) {
    	Object object = null;
    	
    	if (OBJ_ACTIVITY.equalsIgnoreCase(strObjectName))
    		object = m_objActivity;
    	else if (OBJ_LOCATION.equalsIgnoreCase(strObjectName))
    		object = m_objLocation;
    	else if (OBJ_OFFENDER.equalsIgnoreCase(strObjectName))
    		object = m_objOffender;
    	else if (OBJ_COMPLIANCE.equalsIgnoreCase(strObjectName))
    		object = m_objCompliance;
    	else if (OBJ_COMPLIANCEVALUE.equalsIgnoreCase(strObjectName))
    		object = m_objComplianceValue;
    	else if (OBJ_STAFF.equalsIgnoreCase(strObjectName))
    		object = m_objStaff;
    	
    	return object;
    }

    
    /**
     * almost there.  Have to accommodate the fact that not every index is created
     * based on the table's GUID.  e.g., offender index is based on barcode lookup.
     * @param obj
     * @param strKey
     * @return
     */
    private Object getProductionObject(Object obj, String strKey) {
    	Object objProduction = null;
    	
    	if (obj instanceof Offender) {
    		objProduction = m_dataProduction.getOffenders().get(strKey);
    	}
    	
    	return objProduction;
    }
    
    private int m_Counter = 0;
	private boolean verify(Object obj, DataInterface data) {
		boolean bVerified = false;

//		if (obj != null) {
			Class cls = obj.getClass();
			Method[] methods = cls.getDeclaredMethods();
			
			Method methodToInvoke = null;
			
			// Find the method for the field being set
			int nMethodCount = methods.length;
			for (int i = 0; i < nMethodCount; ++i) {
				Method m = methods[i];
				
				String strMethodName = m.getName();
				String strDesiredPackage = obj.getClass().getPackage().getName();
				String  strDesiredClassName = obj.getClass().getName().substring(strDesiredPackage.length() + 1);
				String strDesiredMethodName = "get" + strDesiredClassName + "Id";
				if (strMethodName.equalsIgnoreCase(strDesiredMethodName)) {
					methodToInvoke = m;
					try {
						if (methodToInvoke.getReturnType().getName().endsWith("String")) {
							String strKey = (String)methodToInvoke.invoke(obj, null);
//							++ m_Counter;
//							System.out.println(Integer.valueOf(m_Counter).toString() + ". " + obj.getClass().getName() + " GUID: " + strKey);
							
							// get production object
							Object objFromProductionMap = getProductionObject(obj, strKey);

							// compare string members of current object with that of production object
							verifyObjectToObject(obj, objFromProductionMap);
						}
					}
					catch (Exception e) {
	//					System.out.println(e.getStackTrace());
						// TODO: how to avoid Location.LIST, which is causing the exception
					}
				}
			}
//		}
		return bVerified;
	}
	
	/**
	 * This method compares the string values of the reference object to that
	 * of the production object
	 * @param oReference
	 * @param oProduction
	 * @return
	 */
	protected boolean verifyObjectToObject(Object oReference, Object oProduction) {
		boolean bVerified = true;
		Class cls = oReference.getClass();
		Method[] methods = cls.getDeclaredMethods();
		Method methodRefernceToInvoke = null;
		Method methodProductionToInvoke = null;
		
		// Find the method for the field being set
		int nMethodCount = methods.length;
		for (int i = 0; i < nMethodCount  && bVerified; ++i) {
			Method m = methods[i];
			
			if (m.getName().startsWith("get")) {
				try {
					methodProductionToInvoke = oProduction.getClass().getDeclaredMethod(m.getName(), null);
					String strReferenceValue = (String)m.invoke(oReference, objEmptyString);
					String strProductionValue = (String)methodProductionToInvoke.invoke(oProduction, null);
				}
				catch (Exception e) {
//					System.out.println(e.getStackTrace());
					// TODO: how to avoid Location.LIST, which is causing the exception
				}
			}
		}
		
		return bVerified;
		
	}
	

}

package com.tippingpoint.handheld.data;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeMap;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.tippingpoint.util.xml.SaxBaseHandler;

public class Data implements DataInterface {
	protected static final String INDEX_ACTIVITY = "activity";
	protected static final String INDEX_COMPLIANCE = "compliance";
	protected static final String INDEX_LOCATION = "location";
	protected static final String INDEX_LOCATIONBYOFFENDER = "offenderloc";  // MAP workaround
	protected static final String INDEX_OFFENDER = "offender";
	protected static final String INDEX_STAFF = "staff";

	XMLReader m_xmlreader;

	// general storage
	private HashMap m_hashLookup = new HashMap(); // Populated from root map data, provides index of objects
	private HashMap m_hashRoot = new HashMap();  // Contains lists of all objects represented in XML
	private Stack m_stackCurrObj = new Stack();

	// custom storage TODO: better name in comment please
	private ArrayList m_listScannablesForLogging = new ArrayList();  // Array of type Scannable
	private String m_strCurrentBarcode;
	private String m_strFeedback;
	protected String m_strScannerXmlPath;
	private Staff m_staff;  // when null, show login screen
	
	// Persistence
	private LogEntry m_log = new LogEntry();
	
	public Data(String strFilename) {
		m_strScannerXmlPath = strFilename;
	}
	
	public void clear() {
		// Activities
		HashMap hm = (HashMap)m_hashLookup.get(INDEX_ACTIVITY);
		hm.clear();
		
		// ComplianceConfigurations
		hm = (HashMap)m_hashLookup.get(INDEX_COMPLIANCE);
		Iterator i = hm.values().iterator();
		while(i.hasNext()) {
			ComplianceConfiguration cc = (ComplianceConfiguration)i.next();
			cc.clear();
		}
		hm.clear();
		
		// Locations
		hm = (HashMap)m_hashLookup.get(INDEX_LOCATION);
		i = hm.values().iterator();
		while(i.hasNext()) {
			Location loc = (Location)i.next();
			loc.clear();
		}
		hm.clear();
		
		// Locations found by offender
		hm = (HashMap)m_hashLookup.get(INDEX_LOCATIONBYOFFENDER);
		hm.clear();
		
		// Offenders
		hm = (HashMap)m_hashLookup.get(INDEX_OFFENDER);
		hm.clear();
		
		// Staff
		hm = (HashMap)m_hashLookup.get(INDEX_STAFF);
		hm.clear();
		
		m_hashLookup.clear();
		m_hashRoot.clear();
		m_stackCurrObj.clear();
		
		System.out.println("hash maps and indices cleared");
	}
	
	/**
	 * Once this method has completed successfully, all the data in the XML file
	 * can be found in memory in hashmaps, some of which behave as lookup indices.
	 */
	public void parse() {
		try {
			// The current object is the root hash map
			m_stackCurrObj.push(m_hashRoot);
			m_xmlreader = XMLReaderFactory.createXMLReader();
			SaxBaseHandler saxHandler = new HandheldXmlHandler(null, m_xmlreader, this);
			
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
			populateLookupMaps();
			System.out.println("done loading configuration.");
		}
		catch (Exception e) {
			System.out.println("Failed to create XMLReader");
		}
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

	public HashMap getActivities() { return (HashMap)m_hashLookup.get(INDEX_ACTIVITY); }
	public ArrayList getActivityList() { return (ArrayList)m_hashRoot.get(HandheldXmlHandler.OBJ_ACTIVITY); }
	public String getBarcode() { return m_strCurrentBarcode; }
	public HashMap getCompliance() { return (HashMap)m_hashLookup.get(INDEX_COMPLIANCE); }
	
	public Object getCurrentObject() {
		return m_stackCurrObj.peek();
	}
	
	public Staff getLoggedInStaff() { return m_staff; }
	public String getFeedback() { return m_strFeedback;	}
	public Location getLocationByOffender(Offender offender) {return (Location)((HashMap)m_hashLookup.get(INDEX_LOCATIONBYOFFENDER)).get(offender.getOffenderId()); }
	public HashMap getLocations() { return (HashMap)m_hashLookup.get(INDEX_LOCATION); }
	public LogEntry getLogEntry() { return m_log; }
	public HashMap getOffenders() { return (HashMap)m_hashLookup.get(INDEX_OFFENDER); }
	public ArrayList getScannables() { return m_listScannablesForLogging; }
	public HashMap getStaffs() { return (HashMap)m_hashLookup.get(INDEX_STAFF); }
	
	public Staff getStaffByBarcode(String strBarcode) { return (Staff)getStaffs().get(strBarcode); }
	
	public Object popObject() {
		return m_stackCurrObj.pop();
	}
	
	/**
	 * The scannables that are populated here rely on knowledge of the
	 *  activity scan type.  For instance, a 'bedding exchange' or a
	 *  'shower' requires a single offender scan whereas a 'security check'
	 *  or a 'cell check' requires a cell scan.
	 *  
	 *  TODO: This method is where we can catch the wrong scan type and
	 *  give feedback to the officer.  eg. Officer scanned a cell barcode
	 *  when the activity was 'clothing exchange' -> tell officer to
	 *  scan offender instead.
	 *  @param strBarcode can be either an offender or a location
	 *  @param activity the currently selected activity 
	 * @return
	 */
	public ArrayList populateScannables(String strBarcode, Activity activity) {
		clearScannables();
		setFeedback("");
		
		if (activity.isOffenderScan()) {
			Offender o = getOffenderByBarcode(strBarcode);
			if (o != null)
				addScannable(o);
			else {
				setFeedback(strBarcode + " EXPECTING: offender scan");
			}
		}
		else if (activity.isCellScan() && activity.isOffenderCompliance()) {
	    	Location l = getLocationByBarcode(strBarcode);
	    	
			if (l == null) {
				setFeedback(strBarcode + " EXPECTING: location scan");
			}
			else {
				Iterator i = l.getOffenders().iterator();
				while (i.hasNext()) {
					Offender o = (Offender)i.next();
					addScannable(o);
					System.out.println("added scannable offender: " + o.getName());
				}
			}
		}
		else if (activity.isCellScan() && activity.isCellCompliance()) {
	    	Location l = getLocationByBarcode(strBarcode);
	    	
			if (l == null) {
				setFeedback(strBarcode + " EXPECTING: location scan");
			}
			else
				addScannable(l);
		}
		else
			setFeedback("BUG: undetermined scan type");

		
		return m_listScannablesForLogging; 
	}
	
	// TODO: Have a validation method that runs through each collection
	// (e.g. list or map) contained by the root map.  The validation method
	// should check that each object it contains is of the specified 
	// type.  For example, the activity list should only contain activity
	// objects.  This validation method should log an entry for each
	// invalid object and the type of that object for debugging purposes.
	// It would also be useful if each invalid object could have its key logged.
	
	/**
	 * This method resets state to the beginning.  It is typically called after
	 * a successful record operation.
	 */
	public void reset() {
		setBarcode("<ready for scan>");
		m_listScannablesForLogging.clear();
		m_strFeedback = "";
	}
	
//	public void saveActivity(Activity a) { ; /* asdf */ }
//	public void saveCompliance(ComplianceConfiguration compliance){ ; /* asdf */ }
//	public void saveLocation(Location location){ ; /* asdf */ }
//	public void saveOffender(Offender offender){ ; /* asdf */ }
	public void setBarcode(String strBarcode){ m_strCurrentBarcode = strBarcode; }
	public void clearLoggedInStaff() { setLoggedInStaff(null); }
	public void setLoggedInStaff(Staff staff) { m_staff = staff; }
	
	private void addScannable(Location l) {
		Scannable s = new Scannable();
		
		s.setObject(l);
		m_listScannablesForLogging.add(s);
	}
	
	private void addScannable(Offender o) {
		Scannable s = new Scannable();
		
		s.setObject(o);
		m_listScannablesForLogging.add(s);
	}
	
	/**
	 * For tables that contain a DisplayOrder column, this method will order
	 * the objects in order.  This method assumes unique values for DisplayOrder.
	 * As an example, the compliance value table contains values for multiple
	 * compliance configurations, thus there can be duplicate values for
	 * DisplayOrder.  However, the intent of this method is that it would be
	 * used to order only those compliance values for a single compiance
	 * configuration in which case all the DisplayOrder values would be unique.
	 * @param arrObjects is an ArrayList<DisplayOrderInterface>
	 */
	private void applyDisplayOrder(ArrayList arrObjects) {
		Iterator i = arrObjects.iterator();
		TreeMap tm = new TreeMap(new IntegerComparator());	// use: sort on display order
		while (i.hasNext()) {
			DisplayOrderInterface displayOrderObject = (DisplayOrderInterface)i.next();
			String strDisplayOrder = displayOrderObject.getDisplayOrder();
			if (strDisplayOrder != null)
				tm.put(new Integer(strDisplayOrder), displayOrderObject);
			else
				System.out.println("applyDisplayOrder() - getDisplayOrder() returned null");
		}
		i = tm.values().iterator();
		arrObjects.clear();
		while (i.hasNext()) {
			DisplayOrderInterface displayOrderObject = (DisplayOrderInterface)i.next();
			arrObjects.add(displayOrderObject);		// use: display/combo
		}
	}
	
	/**
	 * Destroys temporary data in the scannable list (e.g. compliance control)
	 */
	private void clearScannables() {
		Iterator i = m_listScannablesForLogging.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			
			if (o instanceof Scannable) {
				Scannable s = (Scannable)o;
				
				s.clearComplianceControl();
			}
				
		}
		
		m_listScannablesForLogging.clear();
	}
	
	private Location getLocationByBarcode(String strBarcode) { return (Location)getLocations().get(strBarcode); }
	
	private Offender getOffenderByBarcode(String strBarcode) { return (Offender)getOffenders().get(strBarcode); }
	 
	private void populateLookupMaps() {
		// Define the indexes
		m_hashLookup.put(INDEX_ACTIVITY, new HashMap());
		m_hashLookup.put(INDEX_COMPLIANCE, new HashMap());
		m_hashLookup.put(INDEX_LOCATION, new HashMap());
		m_hashLookup.put(INDEX_LOCATIONBYOFFENDER, new HashMap());
		m_hashLookup.put(INDEX_OFFENDER, new HashMap());
		m_hashLookup.put(INDEX_STAFF, new HashMap());
		
		// Populate the activity index
		HashMap mapCurr = (HashMap)m_hashLookup.get(INDEX_ACTIVITY);
		ArrayList arrActivity = (ArrayList)m_hashRoot.get(HandheldXmlHandler.OBJ_ACTIVITY);
		Iterator i = arrActivity.iterator();
		while (i.hasNext()) {
			Activity activity = (Activity)i.next();
			mapCurr.put(activity.getName(), activity);
		}
		applyDisplayOrder(arrActivity);
		System.out.println("Index Created: ACTIVITY");
		
		// Populate the compliance index
		mapCurr = (HashMap)m_hashLookup.get(INDEX_COMPLIANCE);
		ArrayList arrCompliance = (ArrayList)m_hashRoot.get(HandheldXmlHandler.OBJ_COMPLIANCE);
		i = arrCompliance.iterator();
		while (i.hasNext()) {
			ComplianceConfiguration configuration = (ComplianceConfiguration)i.next();
			mapCurr.put(configuration.getComplianceId(), configuration);
			applyDisplayOrder(configuration.getValues());
		}
		System.out.println("Index Created: COMPLIANCE");
		
		// Populate the location index
		mapCurr = (HashMap)m_hashLookup.get(INDEX_LOCATION);
		ArrayList arrLocation = (ArrayList)m_hashRoot.get(HandheldXmlHandler.OBJ_LOCATION);
		i = arrLocation.iterator();
		while (i.hasNext()) {
			Location location = (Location)i.next();
			mapCurr.put(location.getBarcode(), location);
		}
		System.out.println("Index Created: LOCATION");
		
		// Populate the location by offender index
		HashMap mapLocations = (HashMap)m_hashLookup.get(INDEX_LOCATIONBYOFFENDER);
		HashMap mapOffenders = (HashMap)m_hashLookup.get(INDEX_OFFENDER);
		arrLocation = (ArrayList)m_hashRoot.get(HandheldXmlHandler.OBJ_LOCATION);
		i = arrLocation.iterator();
		while (i.hasNext()) {
			Location location = (Location)i.next();
			Iterator iOffender = location.getOffenders().iterator();
			while (iOffender.hasNext()) {
				Offender offender = (Offender)iOffender.next();
				mapLocations.put(offender.getOffenderId(), location);
				mapOffenders.put(offender.getBarcode(), offender);
			}
		}
		System.out.println("Index Created: LOCATIONBYOFFENDER & OFFENDER");
		
		// Populate the staff index
		mapCurr = (HashMap)m_hashLookup.get(INDEX_STAFF);
		ArrayList arrStaff = (ArrayList)m_hashRoot.get(HandheldXmlHandler.OBJ_STAFF);
		i = arrStaff.iterator();
		while (i.hasNext()) {
			Staff staff = (Staff)i.next();
			mapCurr.put(staff.getBarcode(), staff);
		}
		System.out.println("Index Created: STAFF");
	}
	
	private void setFeedback(String strFeedback) { m_strFeedback = strFeedback; }

	class IntegerComparator implements Comparator {
        public int compare(Object o1,Object o2)
        {
        	Integer i1 = (Integer)o1;
        	Integer i2 = (Integer)o2;
            return i1.compareTo(i2);
        }
    }
	
}

package com.tippingpoint.handheld.data;

import java.awt.Choice;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.tippingpoint.handheld.ui.DataChoice;
import com.tippingpoint.util.xml.SaxBaseHandler;

public class LegacyData implements DataInterface {
	XMLReader m_xmlreader;
	
	// Legacy Storage
	private HashMap m_hashCompliance = new HashMap();
	private HashMap m_hashLocationByBarcode = new HashMap();
	private HashMap m_hashLocationByOffendersBarcode = new HashMap();
	private HashMap m_hashOffenderByBarcode = new HashMap();
	private ArrayList m_listActivities = new ArrayList();
	private ArrayList m_listScannablesForLogging = new ArrayList();  // Array of type Scannable
	private String m_strCurrentBarcode;
	private String m_strFeedback;

	// Persistence
	private LogEntry m_log = new LogEntry();
	
	public LegacyData(String strFilename) {
		try {
			m_xmlreader = XMLReaderFactory.createXMLReader();
			SaxBaseHandler saxHandler = new LegacyHandheldXmlHandler(null, m_xmlreader, this);
			
			m_xmlreader.setContentHandler(saxHandler);
			m_xmlreader.setErrorHandler(saxHandler);
			FileReader reader = new FileReader(strFilename);
			// TODO: there is a null pointer exception during startup if the
			// path to the XML config is incorrect in the .LNK shortcut file.
			// The 'reader' is not the culprit since this println is not in the output.
			if (reader == null)
				System.out.println("XML configuration file missing.");
				
			m_xmlreader.parse(new InputSource(reader));
			populateLocationsByOffendersBarcode(m_hashLocationByOffendersBarcode, m_hashLocationByBarcode);
		}
		catch (Exception e) {
			System.out.println("Failed to create XMLReader");
		}
		System.out.println("done loading configuration.");
	}

	public ArrayList getActivities() { return m_listActivities; }
	public HashMap getAllOffenders() { return m_hashOffenderByBarcode; }
	public String getBarcode() { return m_strCurrentBarcode; }
	public ComplianceConfiguration getComplianceConfiguration(String strId) { return (ComplianceConfiguration)m_hashCompliance.get(strId); }
	public String getFeedback() { return m_strFeedback;	}
	public Location getLocationByBarcode(String strBarcode) { return (Location)m_hashLocationByBarcode.get(strBarcode); }
	public Location getLocationByOffendersBarcode(String strOffenderBarcode) { return (Location)m_hashLocationByOffendersBarcode.get(strOffenderBarcode); }
	public LogEntry getLogEntry() { return m_log; }
	public Offender getOffenderByBarcode(String strBarcode) { return (Offender)m_hashOffenderByBarcode.get(strBarcode); }

	public ArrayList getScannables() { return m_listScannablesForLogging; }
	
	
	/**
	 * This method resets state to the beginning.  It is typically called after
	 * a successful record operation.
	 */
	public void reset() {
		setBarcode("<ready for scan>");
		m_listScannablesForLogging.clear();
		m_strFeedback = "";
	}
	
	public void saveActivity(Activity a) { m_listActivities.add(a); }
	public void saveCompliance(ComplianceConfiguration compliance) { m_hashCompliance.put(compliance.getComplianceId(), compliance); }
	public void saveLocation(Location location) { 
		m_hashLocationByBarcode.put(location.getBarcode(), location);
	}
	public void saveOffender(Offender offender) { m_hashOffenderByBarcode.put(offender.getBarcode(), offender); }
	public void setBarcode(String strBarcode) { m_strCurrentBarcode = strBarcode; }
	public void setFeedback(String strFeedback) { m_strFeedback = strFeedback; }
	
    public void populateActivities(Choice c, ArrayList listActivities) {
    	Iterator iActivities = listActivities.iterator();
    	while (iActivities.hasNext()) {
    		Activity a = (Activity)iActivities.next();
    		
    		c.add(a.getName());
    	}
    }

    /**
     * This method populates the compliance list based on the compliance type
     * of a given activity.
     * @param choice
     * @param listActivities
     */
    public void populateCompliance(DataChoice choice, Activity a) {
    	ComplianceConfiguration complianceConfiguration = (ComplianceConfiguration)m_hashCompliance.get(a.getComplianceId());
    	ArrayList listValues = complianceConfiguration.getValues();
    	Iterator i = listValues.iterator();
    	choice.removeAll();
    	
    	String strDefault = null;
    	while(i.hasNext()) {
    		ComplianceValue complianceValue = (ComplianceValue)i.next();
    		
    		if (complianceValue.getDefault())
    			strDefault = complianceValue.getValue();
    		choice.add(complianceValue.getValue(), complianceValue);
    	}
    	
    	if (strDefault == null)
    		choice.select(0);
    	else
    		choice.select(strDefault);
    	
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
				setFeedback("EXPECTING: offender scan");
			}
		}
		else if (activity.isCellScan() && activity.isOffenderCompliance()) {
	    	Location l = getLocationByBarcode(strBarcode);
	    	
			if (l == null) {
				setFeedback("EXPECTING: location scan");
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
				setFeedback("EXPECTING: location scan");
			}
			else
				addScannable(l);
		}
		else
			setFeedback("BUG: undetermined scan type");

		
		return m_listScannablesForLogging; 
	}

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
	
    /**
     * Traverse the collection of locations.  For each location, traverse its collection of offenders.
     * For each offender, insert an entry into a map so that the offenders location can be retrieved.
     * @param m_hashLocationByOffendersBarcode
     * @param m_hashLocationByBarcode
     * @return
     */
	private HashMap populateLocationsByOffendersBarcode(HashMap m_hashLocationByOffendersBarcode, HashMap m_hashLocationByBarcode) {
		Collection c = m_hashLocationByBarcode.values();
		Iterator iLocations = c.iterator();
		
		// Process all locations
		while (iLocations.hasNext()) {
			Location locationCurr = (Location)iLocations.next();
			ArrayList arr = locationCurr.getOffenders();
			Iterator iOffenders = arr.iterator();
			
			// Process all offenders for the current location
			while (iOffenders.hasNext()) {
				Offender o = (Offender)iOffenders.next();
				
				m_hashLocationByOffendersBarcode.put(o.getBarcode(), locationCurr);
			}
		}
		
		return m_hashLocationByOffendersBarcode;
	}

}

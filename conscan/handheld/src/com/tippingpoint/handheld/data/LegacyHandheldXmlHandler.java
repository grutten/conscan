package com.tippingpoint.handheld.data;

import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import com.tippingpoint.util.xml.SaxBaseHandler;

/**
 * This class parses the grammar based on the original sample XML file sent to 
 * the handheld.  The syntax contained in the original file was typed in by
 * hand and was created since we didn't have a way to generate the XML from
 * the database yet.
 * @author mgee
 *
 */
public class LegacyHandheldXmlHandler extends SaxBaseHandler {
	
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
	protected static final String TAG_DEFAULT = "isdefault";
	protected static final String TAG_LOCATION = "location";
	protected static final String TAG_LOCATIONS = "locations";
	protected static final String TAG_NAME = "name";
	protected static final String TAG_OFFENDER = "offender";
	protected static final String TAG_OFFENDERS = "offenders";
	protected static final String TAG_SCANTYPE = "scantype";
	protected static final String TAG_TIER = "tier";
	protected static final String TAG_VALUE = "value";
	
	//	private Data m_data;
	private String m_strTierName;
	
	LegacyHandheldXmlHandler(SaxBaseHandler parentHandler, XMLReader reader, LegacyData d) {
		super(parentHandler, reader);
		
		m_data = d;
	}


    public void endElement (String uri, String name, String qName) {
		if (TAG_NAME.equalsIgnoreCase(name)) {
			m_strTierName = m_strCurrentTagValue;
		    handheldLog("     -->" + m_strTierName + "<--");
		}
		
		logEndElement("LegacyHandheld", uri, name, qName);
    }

    public String getName() {	return "root handler"; }
    public String toString() { return getName(); }
    
	
    public void startElement (String uri, String name, String qName, Attributes atts) {
    	if (TAG_LOCATIONS.equalsIgnoreCase(name))
    		pushHandler(new LocationHandler(this, getReader(), getData()));
    	if (TAG_COMPLIANCECONFIGURATIONS.equalsIgnoreCase(name))
    		pushHandler(new ComplianceConfigurationHandler(this, getReader(), getData()));
    	if (TAG_ACTIVITIES.equalsIgnoreCase(name))
    		pushHandler(new ActivityHandler(this, getReader(), getData()));
    	
    	
		logStartElement("LegacyHandheld", uri, name, qName);
    }

    protected LegacyData getData() { return m_data; }
	
	protected class ActivityHandler extends LegacyHandheldXmlHandler {
		private String m_strActivityName;
		private String m_strComplianceConfiguration;
		private String m_strActivityId;
		private String m_strScantype;
		private String m_strCompliancetype;
		private Activity m_activity;
		
		ActivityHandler(SaxBaseHandler parentHandler, XMLReader reader, LegacyData d) {
			super(parentHandler, reader, d);
		}

		public void endElement (String uri, String name, String qName) {
			if (TAG_NAME.equalsIgnoreCase(name)) {
				m_strActivityName = m_strCurrentTagValue;
			    handheldLog("     -->" + m_strActivityName + "<--");
			}
			else if (TAG_COMPLIANCETYPE.equalsIgnoreCase(name)) {
				m_strCompliancetype = m_strCurrentTagValue;
			    handheldLog("     -->" + m_strCompliancetype + "<--");
			}
			else if (TAG_SCANTYPE.equalsIgnoreCase(name)) {
				m_strScantype = m_strCurrentTagValue;
			    handheldLog("     -->" + m_strScantype + "<--");
			}
			else if (TAG_ACTIVITY.equalsIgnoreCase(name)) {
				m_activity.setComplianceId(m_strComplianceConfiguration);
				m_activity.setActivityId(m_strActivityId);
				m_activity.setName(m_strActivityName);
				m_activity.setScantype(m_strScantype);
				m_activity.setCompliancetype(m_strCompliancetype);
				getData().saveActivity(m_activity);
			}
			else if (TAG_ACTIVITIES.equalsIgnoreCase(name))
	    		popHandler();
			
			logEndElement("Activity", uri, name, qName);
	    }

		public void startElement (String uri, String name, String qName, Attributes attrs) {
			if (TAG_ACTIVITY.equalsIgnoreCase(name)) {
				m_activity = new Activity();
				// the activity id will be saved later
				m_strActivityId = attrs.getValue(ATTRIBUTE_ACTIVITYID);  
			    handheldLog("     -->" + m_strActivityId + "<--");
			}
				
			logStartElement("Activity", uri, name, qName);
			
			if (TAG_COMPLIANCECONFIGURATION.equalsIgnoreCase(name)) {
				// the compliance id will be saved later
				m_strComplianceConfiguration = attrs.getValue(ATTRIBUTE_COMPLIANCECONFIGURATIONID); 
			    handheldLog("     -->" + m_strComplianceConfiguration + "<--");
			}
	    }
	}
	
	protected class ComplianceConfigurationHandler extends LegacyHandheldXmlHandler {
		private String m_strComplianceConfigurationId;
		private String m_strComplianceConfigurationName;
		private String m_strComplianceValueId;
		private String m_strDefault;
		private String m_strValue;  // the current <value>, not to be confused with <ComplianceValue>
		private ComplianceConfiguration m_complianceConfiguration;

		
		ComplianceConfigurationHandler(SaxBaseHandler parentHandler, XMLReader reader, LegacyData d) {
			super(parentHandler, reader, d);
		}

		public void endElement (String uri, String name, String qName) {
			if (TAG_NAME.equalsIgnoreCase(name)) {
				m_strComplianceConfigurationName = m_strCurrentTagValue;
			    handheldLog("     -->" + m_strComplianceConfigurationName + "<--");
			}
			else if (TAG_COMPLIANCEVALUE.equalsIgnoreCase(name)) {
				ComplianceValue complianceValue = new ComplianceValue();
				complianceValue.setValue(m_strValue);
				complianceValue.setComplianceValueId(m_strComplianceValueId);
				complianceValue.setDefault(VALUE_DEFAULT_TRUE.equalsIgnoreCase(m_strDefault));
				m_complianceConfiguration.addValue(complianceValue);			
			    handheldLog("     -->" + m_strValue + "<--");
			}
			else if (TAG_COMPLIANCECONFIGURATION.equalsIgnoreCase(name)) {
				m_complianceConfiguration.setComplianceId(m_strComplianceConfigurationId);
				m_complianceConfiguration.setName(m_strComplianceConfigurationName);
				getData().saveCompliance(m_complianceConfiguration);
			}
			else if (TAG_COMPLIANCECONFIGURATIONS.equalsIgnoreCase(name))
	    		popHandler();
			else if (TAG_DEFAULT.equalsIgnoreCase(name)) {
				m_strDefault = m_strCurrentTagValue;
			}
			else if (TAG_VALUE.equalsIgnoreCase(name)) {
				m_strValue = m_strCurrentTagValue;
			}
	    	
			logEndElement("ComplianceConfiguration", uri, name, qName);
	    }

		public void startElement (String uri, String name, String qName, Attributes attrs) {
			logStartElement("ComplianceConfiguration", uri, name, qName);
			
			if (TAG_COMPLIANCECONFIGURATION.equalsIgnoreCase(name)) {
				m_complianceConfiguration = new ComplianceConfiguration();
				m_strComplianceConfigurationId = attrs.getValue(ATTRIBUTE_COMPLIANCECONFIGURATIONID);
			    handheldLog("     -->" + m_strComplianceConfigurationId + "<--");
			}
			else if (TAG_COMPLIANCEVALUE.equalsIgnoreCase(name)) {
				m_strComplianceValueId = attrs.getValue(ATTRIBUTE_COMPLIANCEVALUEID);  
			    handheldLog("     -->" + m_strComplianceValueId + "<--");
			}
	    }
	}
	
	protected class LocationHandler extends LegacyHandheldXmlHandler {
		private String m_strLocationId;
		private String m_strLocationName;
		private String m_strLocationBarCode;
		private ArrayList m_listOffeneders;
		
		private Location m_location;
		
		LocationHandler(SaxBaseHandler parentHandler, XMLReader reader, LegacyData d) {
			super(parentHandler, reader, d);
		}

		public void endElement (String uri, String name, String qName) {
			if (TAG_NAME.equalsIgnoreCase(name)) {
				m_strLocationName = m_strCurrentTagValue;
			    handheldLog("     -->" + m_strLocationName + "<--");
			}
			if (TAG_BARCODE.equalsIgnoreCase(name)) {
				m_strLocationBarCode = m_strCurrentTagValue;
			    handheldLog("     -->" + m_strLocationBarCode + "<--");
			}
			else if (TAG_LOCATION.equalsIgnoreCase(name)) {
				m_location.setBarcode(m_strLocationBarCode);
				m_location.setLocationId(m_strLocationId);
				m_location.setName(m_strLocationName);
				m_location.setOffenders(m_listOffeneders);
				getData().saveLocation(m_location);
			}
			else if (TAG_LOCATIONS.equalsIgnoreCase(name))
	    		popHandler();
			
			logEndElement("Location", uri, name, qName);
	    }

		public void startElement (String uri, String name, String qName, Attributes attrs) {
	    	if (TAG_OFFENDERS.equalsIgnoreCase(name))
	    		pushHandler(new OffenderHandler(this, getReader(), getData(), m_listOffeneders));
	    	else if (TAG_LOCATION.equalsIgnoreCase(name)) {
	    		m_location = new Location();
	    		m_listOffeneders = new ArrayList();
				m_strLocationId = attrs.getValue(ATTRIBUTE_LOCATIONID);  
	    	}

			logStartElement("Location", uri, name, qName);
	    }
	}

	protected class OffenderHandler extends LegacyHandheldXmlHandler {
		private String m_strOffenderId;
		private String m_strOffenderName;
		private String m_strBookingNumber;
		private String m_strBarCode;
		private Offender m_offender;
		
		private ArrayList m_listOffenderInLocation;
		
		OffenderHandler(SaxBaseHandler parentHandler, XMLReader reader, LegacyData d, ArrayList listOffenderInLocation) {
			super(parentHandler, reader, d);
			m_listOffenderInLocation = listOffenderInLocation;
		}

		public void endElement (String uri, String name, String qName) {
			if (TAG_NAME.equalsIgnoreCase(name)) {
				m_strOffenderName = m_strCurrentTagValue;
			    handheldLog("     -->" + m_strOffenderName + "<--");
			}
			else if (TAG_BOOKINGNUMBER.equalsIgnoreCase(name)) {
				m_strBookingNumber = m_strCurrentTagValue;
			    handheldLog("     -->" + m_strBookingNumber + "<--");
			}
			else if (TAG_BARCODE.equalsIgnoreCase(name)) {
				m_strBarCode = m_strCurrentTagValue;
			    handheldLog("     -->" + m_strBarCode + "<--");
			}
			else if (TAG_OFFENDER.equalsIgnoreCase(name)) {
				m_offender.setBarcode(m_strBarCode);
				m_offender.setBookingNumber(m_strBookingNumber);
				m_offender.setOffenderId(m_strOffenderId);
				m_offender.setName(m_strOffenderName);
				getData().saveOffender(m_offender);
				m_listOffenderInLocation.add(m_offender);
			}
			else if (TAG_OFFENDERS.equalsIgnoreCase(name))
	    		popHandler();
			
			logEndElement("Offender", uri, name, qName);
	    }

		public void startElement (String uri, String name, String qName, Attributes attrs) {
			if (TAG_OFFENDER.equalsIgnoreCase(name)) {
				m_offender = new Offender();
				m_strOffenderId = attrs.getValue(ATTRIBUTE_OFFENDERID);  
			}
				
			logStartElement("Offender", uri, name, qName);
	    }
	}
	

	
}

package com.tippingpoint.handheld.data;

import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import com.tippingpoint.util.xml.SaxBaseHandler;

public class HandheldXmlHandler extends SaxBaseHandler {
	private boolean m_bLoggingEnabled = false;
	private Data m_data;
	private String m_strTierName;
	
	HandheldXmlHandler(SaxBaseHandler parentHandler, XMLReader reader, Data d) {
		super(parentHandler, reader);
		
		m_data = d;
	}

    public void endDocument () {
    	handheldLog("End document");
    }

    public void endElement (String uri, String name, String qName) {
		if (TAG_NAME.equalsIgnoreCase(name)) {
			m_strTierName = m_strCurrentTagValue;
		    handheldLog("     -->" + m_strTierName + "<--");
		}
		
		logEndElement("Handheld", uri, name, qName);
    }

    public String getName() {	return "root handler"; }
    public String toString() { return getName(); }
    
    public void startDocument () {
    	handheldLog("Start document");
    }
	
    public void startElement (String uri, String name, String qName, Attributes atts) {
    	if (TAG_LOCATIONS.equalsIgnoreCase(name))
    		pushHandler(new LocationHandler(this, getReader(), getData()));
    	if (TAG_COMPLIANCECONFIGURATIONS.equalsIgnoreCase(name))
    		pushHandler(new ComplianceConfigurationHandler(this, getReader(), getData()));
    	if (TAG_ACTIVITIES.equalsIgnoreCase(name))
    		pushHandler(new ActivityHandler(this, getReader(), getData()));
    	
    	
		logStartElement("Handheld", uri, name, qName);
    }

    protected Data getData() { return m_data; }
	
	protected class ActivityHandler extends HandheldXmlHandler {
		private String m_strActivityName;
		private String m_strComplianceConfiguration;
		private String m_strScantype;
		private Activity m_activity;
		
		ActivityHandler(SaxBaseHandler parentHandler, XMLReader reader, Data d) {
			super(parentHandler, reader, d);
		}

		public void endElement (String uri, String name, String qName) {
			if (TAG_NAME.equalsIgnoreCase(name)) {
				m_strActivityName = m_strCurrentTagValue;
			    handheldLog("     -->" + m_strActivityName + "<--");
			}
			else if (TAG_SCANTYPE.equalsIgnoreCase(name)) {
				m_strScantype = m_strCurrentTagValue;
			    handheldLog("     -->" + m_strScantype + "<--");
			}
			else if (TAG_ACTIVITY.equalsIgnoreCase(name)) {
				m_activity.setComplianceId(m_strComplianceConfiguration);
				m_activity.setName(m_strActivityName);
				m_activity.setScantype(m_strScantype);
				getData().saveActivity(m_activity);
			}
			else if (TAG_ACTIVITIES.equalsIgnoreCase(name))
	    		popHandler();
			
			logEndElement("Activity", uri, name, qName);
	    }

		public void startElement (String uri, String name, String qName, Attributes attrs) {
			if (TAG_ACTIVITY.equalsIgnoreCase(name))
				m_activity = new Activity();
				
			logStartElement("Activity", uri, name, qName);
			
			if (TAG_COMPLIANCECONFIGURATION.equalsIgnoreCase(name)) {
				m_strComplianceConfiguration = attrs.getValue(ATTRIBUTE_ID);
			    handheldLog("     -->" + m_strComplianceConfiguration + "<--");
			}
	    }
	}
	
	protected class ComplianceConfigurationHandler extends HandheldXmlHandler {
		private String m_strComplianceConfigurationId;
		private String m_strComplianceConfigurationName;
		private ComplianceConfiguration m_complianceConfig;

		
		ComplianceConfigurationHandler(SaxBaseHandler parentHandler, XMLReader reader, Data d) {
			super(parentHandler, reader, d);
		}

		public void endElement (String uri, String name, String qName) {
			if (TAG_NAME.equalsIgnoreCase(name)) {
				m_strComplianceConfigurationName = m_strCurrentTagValue;
			    handheldLog("     -->" + m_strComplianceConfigurationName + "<--");
			}
			else if (TAG_VALUE.equalsIgnoreCase(name)) {
				m_complianceConfig.addValue(m_strCurrentTagValue);			
			    handheldLog("     -->" + m_strCurrentTagValue + "<--");
			}
			else if (TAG_COMPLIANCECONFIGURATION.equalsIgnoreCase(name)) {
				m_complianceConfig.setId(m_strComplianceConfigurationId);
				m_complianceConfig.setName(m_strComplianceConfigurationName);
				getData().saveCompliance(m_complianceConfig);
			}
			else if (TAG_COMPLIANCECONFIGURATIONS.equalsIgnoreCase(name))
	    		popHandler();
	    	
			logEndElement("Compliance", uri, name, qName);
	    }

		public void startElement (String uri, String name, String qName, Attributes attrs) {
			logStartElement("Compliance", uri, name, qName);
			
			if (TAG_COMPLIANCECONFIGURATION.equalsIgnoreCase(name)) {
				m_complianceConfig = new ComplianceConfiguration();
				m_strComplianceConfigurationId = attrs.getValue(ATTRIBUTE_ID);
			    handheldLog("     -->" + m_strComplianceConfigurationId + "<--");
			}
	    }
	}
	
	protected class LocationHandler extends HandheldXmlHandler {
		private String m_strLocationName;
		private String m_strLocationBarCode;
		private ArrayList m_listOffeneders;
		
		private Location m_location;
		
		LocationHandler(SaxBaseHandler parentHandler, XMLReader reader, Data d) {
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
				m_location.setName(m_strLocationName);
				m_location.setOffenders(m_listOffeneders);
				getData().saveLocation(m_location);
			}
			else if (TAG_LOCATIONS.equalsIgnoreCase(name))
	    		popHandler();
			
			logEndElement("Location", uri, name, qName);
	    }

		public void startElement (String uri, String name, String qName, Attributes atts) {
	    	if (TAG_OFFENDERS.equalsIgnoreCase(name))
	    		pushHandler(new OffenderHandler(this, getReader(), getData(), m_listOffeneders));
	    	else if (TAG_LOCATION.equalsIgnoreCase(name)) {
	    		m_location = new Location();
	    		m_listOffeneders = new ArrayList();
	    	}

			logStartElement("Location", uri, name, qName);
	    }
	}

	protected class OffenderHandler extends HandheldXmlHandler {
		private String m_strOffenderName;
		private String m_strBookingNumber;
		private String m_strBarCode;
		private Offender m_offender;
		
		private ArrayList m_listOffenderInLocation;
		
		OffenderHandler(SaxBaseHandler parentHandler, XMLReader reader, Data d, ArrayList listOffenderInLocation) {
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
				m_offender.setName(m_strOffenderName);
				getData().saveOffender(m_offender);
				m_listOffenderInLocation.add(m_offender);
			}
			else if (TAG_OFFENDERS.equalsIgnoreCase(name))
	    		popHandler();
			
			logEndElement("Offender", uri, name, qName);
	    }

		public void startElement (String uri, String name, String qName, Attributes atts) {
			if (TAG_OFFENDER.equalsIgnoreCase(name))
				m_offender = new Offender();
				
			logStartElement("Offender", uri, name, qName);
	    }
	}
	
	protected void logEndElement(String strTitle, String uri, String name, String qName) {
		if ("".equals (uri))
			handheldLog("End element (" + strTitle + "): " + qName);
		else
			handheldLog("End element (" + strTitle + "): {" + uri + "}" + name);
		
	}
	protected void logStartElement(String strTitle, String uri, String name, String qName) {
		if ("".equals (uri))
			handheldLog("Start element (" + strTitle + "): " + qName);
		else
			handheldLog("Start element (" + strTitle + "): {" + uri + "}" + name);
		
	}

	protected void handheldLog(String str) { if (m_bLoggingEnabled) System.out.println(str); }
	
}

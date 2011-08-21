package com.tippingpoint.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.conscan.servlet.Services;
import com.tippingpoint.sql.SqlBaseException;

public class GenericXmlHandler extends SaxBaseHandler {
	private static Log m_log = LogFactory.getLog(GenericXmlHandler.class);
	private final DataInterface m_data;
	private BusinessObject m_objCurrentObject;
	private String m_strCurrentFieldName;

	public GenericXmlHandler(final SaxBaseHandler parentHandler, final XMLReader reader, final DataInterface d) {
		super(parentHandler, reader);

		m_data = d;
	}

	@Override
	public void endElement(final String uri, final String name, final String qName) {
		if (Services.TAG_FIELD.equals(qName)) {
			m_log.debug("Field : " + m_strCurrentFieldName + " - " + m_strCurrentTagValue);
			if (m_objCurrentObject != null) {
				m_objCurrentObject.setValue(m_strCurrentFieldName, m_strCurrentTagValue);
				System.out.println("FIELD IDENTIFIED: " + m_strCurrentFieldName);
				m_strCurrentFieldName = null;
			}
			else {
				m_log.debug("Unexpected null object in GenericXmlHandler - field:" + m_strCurrentTagValue);
			}
		}
		else if (Services.TAG_OBJECT.equals(qName)) {
			if (m_objCurrentObject != null) {
				try {
					m_objCurrentObject.save();
					m_data.add(m_objCurrentObject);
				}
				catch (final SqlBaseException e) {
					m_log.debug("Unexpected SqlBaseException exception saving Business Object: " + e.toString());
				}
				catch (final Exception e) {
					m_log.debug("Unexpected exception saving Business Object: " + e.toString());
				}
			}
			else {
				m_log.debug("Unexpected null object in GenericXmlHandler");
			}
		}
	}

	@Override
	public void startElement(final String uri, final String name, final String qName, final Attributes attrs) {
		final String strCurrObjName = attrs.getValue("name");

		if (Services.TAG_OBJECT.equals(qName)) {
			final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(strCurrObjName);

			if (builder == null) {
				m_log.debug("NOT FOUND the following object specified in XML could not be found in the schema: " +
						strCurrObjName);
			}
			else {
				m_objCurrentObject = builder.get();
				if (m_objCurrentObject == null) {
					m_log.debug("GenericXmlHandler: unexpected object creation failure - " + strCurrObjName);
				}
				else {
					m_data.setObjectName(strCurrObjName);
					m_log.debug("GenericXmlHandler: successful object creation - " + strCurrObjName);
				}
			}
		}
		else if (Services.TAG_FIELD.equals(qName)) {
			m_strCurrentFieldName = strCurrObjName;
		}
	}
}

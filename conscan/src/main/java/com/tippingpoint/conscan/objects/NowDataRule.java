package com.tippingpoint.conscan.objects;

import java.util.Date;
import java.util.Map;

/**
 * This class assigns the current date to the specified column.
 */
public final class NowDataRule implements DataRule {
	/** This member holds the name of the field to populate. */
	private final String m_strName;
	
	/** This member holds the flag indicating that the value should always be updated. */
	private final boolean m_bForce;

	/**
	 * This method constructs a new rule for the named data value.
	 */
	public NowDataRule(final String strName) {
		this(strName, true);
	}

	/**
	 * This method constructs a new rule for the named data value.
	 */
	public NowDataRule(final String strName, boolean bForce) {
		m_strName = strName;
		m_bForce = bForce;
	}

	/**
	 * This method applies the rule.
	 */
	@Override
	public void apply(final Map<String, FieldValue> mapValues) {
		FieldValue fieldValue = mapValues.get(m_strName);
		if (fieldValue == null) {
			fieldValue = new FieldValue(m_strName);
			mapValues.put(m_strName, fieldValue);
		}

		if (fieldValue.getValue() == null || m_bForce) {
			fieldValue.setValue(new Date());
		}
	}
}

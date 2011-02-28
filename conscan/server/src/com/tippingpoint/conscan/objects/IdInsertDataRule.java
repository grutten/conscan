package com.tippingpoint.conscan.objects;

import java.util.Map;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnTypeId;

/**
 * This rule adds a new ID if it is not there on save.
 */
public class IdInsertDataRule implements DataRule {
	/** This member holds the id column. */
	private Column m_columnId;
	
	/**
	 * This method constructs a new rule for the given column.
	 */
	public IdInsertDataRule(Column columnId) {
		if (!(columnId.getType() instanceof ColumnTypeId)) {
			throw new IllegalArgumentException("Column '" + columnId.getName() + "' is not an ID column.");
		}

		m_columnId = columnId;
	}
	
	/**
	 * This method applies the rule.
	 */
	@Override
	public void apply(Map<String, FieldValue> mapValues) {
		// if the field does not exist, just insert it into the map
		String strName = m_columnId.getName();
		FieldValue fieldValue = mapValues.get(strName);
		if (fieldValue == null) {
			fieldValue = new FieldValue(strName);
			mapValues.put(strName, fieldValue);
		}
	}
}

package com.tippingpoint.database.parser;

import org.xml.sax.Attributes;
import com.tippingpoint.database.Table;

/**
 * This class is used to parse a reference to a row in another table.
 */
public class ReferenceTableRule extends ImporterRule {
	/**
	 * This method is called when a referenced table specification is encountered.
	 */
	@Override
	public void begin(final String strNamespace, final String strName, final Attributes attributes) throws Exception {
		super.begin(strNamespace, strName, attributes);

		final Importer importer = getImporter();
		if (importer.hasActiveTable()) {
			final Table table = importer.getSchema().getTable(attributes.getValue(ATTRIBUTE_NAME));
			if (table != null) {
				getDigester().push(ColumnValueRule.COLUMN_STACK, new TableValue(table));
			}
		}
	}

	/**
	 * This method is called when the column specification is closed.
	 */
	@Override
	public void end(final String strNamespace, final String strName) throws Exception {
		final Importer importer = getImporter();
		if (importer.hasActiveTable() && !getDigester().isEmpty(ColumnValueRule.COLUMN_STACK)) {
			if (getDigester().peek(ColumnValueRule.COLUMN_STACK) instanceof TableValue) {
				final TableValue tableValue = (TableValue)getDigester().pop(ColumnValueRule.COLUMN_STACK);

				final Object objValue = importer.getValue(tableValue);

				if (!getDigester().isEmpty(ColumnValueRule.COLUMN_STACK)) {
					final Object objNextValue = getDigester().peek(ColumnValueRule.COLUMN_STACK);
					if (objNextValue instanceof ColumnValue) {
						((ColumnValue)objNextValue).setValue(objValue);
					}
					else if (objNextValue instanceof TableValue) {
						final ColumnValue columnValue =
							new ColumnValue(tableValue.getTable().getPrimaryKey().getColumns().next());
						columnValue.setValue(objValue);

						((TableValue)objNextValue).add(columnValue);
					}
				}
			}
		}

		super.end(strNamespace, strName);
	}
}

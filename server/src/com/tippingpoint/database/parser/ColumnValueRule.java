package com.tippingpoint.database.parser;

import org.xml.sax.Attributes;
import com.tippingpoint.database.Column;

/**
 * This class is used as a rule in the import to select the value of a column in the currently selected table.
 */
public final class ColumnValueRule extends ImporterRule {
	static final String COLUMN_STACK = "column.stack";

	/**
	 * This method is called when a column specification is encountered.
	 */
	@Override
	public void begin(final String strNamespace, final String strName, final Attributes attributes) throws Exception {
		super.begin(strNamespace, strName, attributes);

		final Importer importer = getImporter();
		if (importer.hasActiveTable()) {
			// if there is nothing on the column stack, then the column is from the active table
			if (getDigester().isEmpty(COLUMN_STACK)) {
				final Column column = importer.getActiveTable().getColumn(attributes.getValue(ATTRIBUTE_NAME));
				if (column != null) {
					getDigester().push(COLUMN_STACK, new ColumnValue(column));
				}
			}
			else if (getDigester().peek(COLUMN_STACK) instanceof TableValue) {
				final TableValue tableValue = (TableValue)getDigester().peek(COLUMN_STACK);

				final Column column = tableValue.getTable().getColumn(attributes.getValue(ATTRIBUTE_NAME));
				if (column != null) {
					getDigester().push(COLUMN_STACK, new ColumnValue(column));
				}
			}
		}
	}

	/**
	 * This method is called when text is encountered for the column specification.
	 */
	@Override
	public void body(final String strNamespace, final String strName, final String strText) throws Exception {
		super.body(strNamespace, strName, strText);

		if (strText != null && !getDigester().isEmpty(COLUMN_STACK)) {
			if (getDigester().peek(COLUMN_STACK) instanceof ColumnValue) {
				((ColumnValue)getDigester().peek(COLUMN_STACK)).appendText(strText);
			}
		}
	}

	/**
	 * This method is called when the column specification is closed.
	 */
	@Override
	public void end(final String strNamespace, final String strName) throws Exception {
		final Importer importer = getImporter();
		if (importer.hasActiveTable() && !getDigester().isEmpty(COLUMN_STACK)) {
			final ColumnValue columnValue = (ColumnValue)getDigester().pop(COLUMN_STACK);

			if (getDigester().isEmpty(COLUMN_STACK)) {
				importer.setColumnValue(columnValue.getColumn(), columnValue.getValue());
			}
			else if (getDigester().peek(COLUMN_STACK) instanceof TableValue) {
				final TableValue tableValue = (TableValue)getDigester().peek(COLUMN_STACK);

				tableValue.add(columnValue);
			}
		}

		super.end(strNamespace, strName);
	}
}

package com.tippingpoint.database.parser;

import org.xml.sax.Attributes;

/**
 * This rule is used to select the table currently being used for import.
 */
public final class TableSelectRule extends ImporterRule {
	/**
	 * This method selects the table being imported.
	 */
	@Override
	public void begin(final String strNamespace, final String strName, final Attributes attributes) {
		final Importer importer = getImporter();

		final String strTableName = attributes.getValue(ATTRIBUTE_NAME);

		importer.setTable(strTableName);
	}

	/**
	 * This method removes the table being imported.
	 */
	@Override
	public void end(final String strNamespace, final String strName) {
		final Importer importer = getImporter();
		importer.setTable(null);
	}
}

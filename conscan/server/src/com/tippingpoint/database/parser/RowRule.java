package com.tippingpoint.database.parser;

import org.xml.sax.Attributes;

/**
 * This class sets up a row to import using the data collected for a table.
 */
public final class RowRule extends ImporterRule {
	/**
	 * This method clears the data from the last row import.
	 */
	@Override
	public void begin(final String strNamespace, final String strName, final Attributes attributes) {
		final Importer importer = getImporter();

		importer.clearRow();
	}

	/**
	 * This method removes the table being imported.
	 */
	@Override
	public void end(final String strNamespace, final String strName) {
		final Importer importer = getImporter();

		importer.saveRow();
	}
}

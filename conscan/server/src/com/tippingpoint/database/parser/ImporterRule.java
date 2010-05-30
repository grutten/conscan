package com.tippingpoint.database.parser;

/**
 * This class is a common class for the importer rules.
 */
public abstract class ImporterRule extends BaseRule {
	/**
	 * This method returns the importer from the digester stack. It is assumed that the importer the next item on the
	 * digester stack.
	 */
	protected Importer getImporter() {
		Importer importer = null;
		final Object objImporter = getDigester().peek();
		if (objImporter instanceof Importer) {
			importer = (Importer)objImporter;
		}

		return importer;
	}
}

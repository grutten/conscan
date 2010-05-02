package com.tippingpoint.database.parser;

import java.io.IOException;
import java.io.Reader;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;
import com.tippingpoint.database.ColumnType;
import com.tippingpoint.database.ColumnTypeConverter;
import com.tippingpoint.database.Schema;

/**
 * This class is used to parse the database XML which defines the configuration of the database.
 */
public final class Parser {
	static {
		ConvertUtils.register(new ColumnTypeConverter(), ColumnType.class);
	}

	/**
	 * This method parses the files and returns a schema for the passed in file. It is assumed that the file contains
	 * XML used to describe the database configuration.
	 * 
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Schema parse(final Reader reader) throws IOException, SAXException {
		final Digester digester = new Digester();
		digester.setValidating(false);
		digester.addObjectCreate("Schema", "com.tippingpoint.database.Schema");
		digester.addSetProperties("Schema");
		digester.addObjectCreate("Schema/Table", "com.tippingpoint.database.Table");
		digester.addSetProperties("Schema/Table");
		digester.addSetNext("Schema/Table", "addTable", "com.tippingpoint.database.Table");
		digester.addObjectCreate("Schema/Table/Column", "com.tippingpoint.database.ColumnDefinition");
		digester.addSetProperties("Schema/Table/Column");
		digester.addSetNext("Schema/Table/Column", "add", "com.tippingpoint.database.ColumnDefinition");
		digester.addRule("Schema/Table/Constraint", new ConstraintRule());
		digester.addSetProperties("Schema/Table/Constraint");
		digester.addSetNext("Schema/Table/Constraint", "add", "com.tippingpoint.database.Constraint");
		digester.addRule("Schema/Table/Constraint/Column", new ConstraintColumnRule());
		digester.addRule("Schema/Table/Constraint/Table", new ForeignTableRule());
		digester.addRule("Schema/Table/Constraint/Table/Column", new ForeignColumnRule());

		return (Schema)digester.parse(reader);
	}

	/**
	 * This method parses the import data XML file.
	 * 
	 * @param reader Reader containing the XML stream for the XML.
	 * @param importer Importer used for importing the data in the reader.
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void parseImport(final Reader reader, final Importer importer) throws IOException, SAXException {
		final Digester digester = new Digester();
		digester.setValidating(false);

		// place the schema in the root of the digester stack
		digester.push(importer);

		digester.addRule("data/Table", new TableSelectRule());
		digester.addRule("data/Table/item", new RowRule());
		digester.addRule("*/Column", new ColumnValueRule());
		digester.addRule("*/Column/Table", new ReferenceTableRule());

		digester.parse(reader);
	}
}

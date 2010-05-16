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
		digester.addObjectCreate("schema", "com.tippingpoint.database.Schema");
		digester.addSetProperties("schema");
		digester.addObjectCreate("schema/table", "com.tippingpoint.database.Table");
		digester.addSetProperties("schema/table");
		digester.addSetNext("schema/table", "addTable", "com.tippingpoint.database.Table");
		digester.addObjectCreate("schema/table/Column", "com.tippingpoint.database.ColumnDefinition");
		digester.addSetProperties("schema/table/Column");
		digester.addSetNext("schema/table/Column", "add", "com.tippingpoint.database.ColumnDefinition");
		digester.addRule("schema/table/Constraint", new ConstraintRule());
		digester.addSetProperties("schema/table/Constraint");
		digester.addSetNext("schema/table/Constraint", "add", "com.tippingpoint.database.Constraint");
		digester.addRule("schema/table/Constraint/Column", new ConstraintColumnRule());
		digester.addRule("schema/table/Constraint/table", new ForeignTableRule());
		digester.addRule("schema/table/Constraint/table/Column", new ForeignColumnRule());

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

		digester.addRule("data/table", new TableSelectRule());
		digester.addRule("data/table/item", new RowRule());
		digester.addRule("*/Column", new ColumnValueRule());
		digester.addRule("*/Column/table", new ReferenceTableRule());

		digester.parse(reader);
	}
}

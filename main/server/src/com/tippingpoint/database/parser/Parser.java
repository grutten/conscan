package com.tippingpoint.database.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.ColumnType;
import com.tippingpoint.database.ColumnTypeConverter;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;

/**
 * This class is used to parse the database XML which defines the configuration of the database.
 */
public final class Parser {
	public static final String TAG_DATA = "data";
	public static final String TAG_ITEM = "item";
	public static final String TAG_ITEMS = "items";

	static {
		ConvertUtils.register(new ColumnTypeConverter(), ColumnType.class);
	}

	/**
	 * This method parses and returns a schema for the passed in stream. It is assumed that the stream contains XML used
	 * to describe the database configuration.
	 * 
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Schema parse(final InputStream is) throws IOException, SAXException {
		return (Schema)getDigester().parse(is);
	}

	/**
	 * This method parses and returns a schema for the passed in stream. It is assumed that the stream contains XML used
	 * to describe the database configuration.
	 * 
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Schema parse(final Reader reader) throws IOException, SAXException {
		return (Schema)getDigester().parse(reader);
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

		digester.addRule(TAG_DATA + "/" + Table.TAG_NAME, new TableSelectRule());
		digester.addRule(TAG_DATA + "/" + TAG_ITEM, new RowRule());
		digester.addRule("*/" + ColumnDefinition.TAG_NAME, new ColumnValueRule());
		digester.addRule("*/" + ColumnDefinition.TAG_NAME + "/" + Table.TAG_NAME, new ReferenceTableRule());

		digester.parse(reader);
	}

	/**
	 * This method returns a digester used to parse the database configuration.
	 */
	private static Digester getDigester() {
		final Digester digester = new Digester();
		digester.setValidating(false);
		digester.addObjectCreate(Schema.TAG_NAME, "com.tippingpoint.database.Schema");
		digester.addSetProperties(Schema.TAG_NAME);
		digester.addObjectCreate(Schema.TAG_NAME + "/" + Table.TAG_NAME, "com.tippingpoint.database.Table");
		digester.addSetProperties(Schema.TAG_NAME + "/" + Table.TAG_NAME);
		digester.addSetNext(Schema.TAG_NAME + "/" + Table.TAG_NAME, "addTable", "com.tippingpoint.database.Table");
		digester.addObjectCreate(Schema.TAG_NAME + "/" + Table.TAG_NAME + "/" + ColumnDefinition.TAG_NAME,
				"com.tippingpoint.database.ColumnDefinition");
		digester.addSetProperties(Schema.TAG_NAME + "/" + Table.TAG_NAME + "/" + ColumnDefinition.TAG_NAME);
		digester.addSetNext(Schema.TAG_NAME + "/" + Table.TAG_NAME + "/" + ColumnDefinition.TAG_NAME, "add",
				"com.tippingpoint.database.ColumnDefinition");
		digester.addRule(Schema.TAG_NAME + "/" + Table.TAG_NAME + "/" + Constraint.TAG_NAME, new ConstraintRule());
		digester.addSetProperties(Schema.TAG_NAME + "/" + Table.TAG_NAME + "/" + Constraint.TAG_NAME);
		digester.addSetNext(Schema.TAG_NAME + "/" + Table.TAG_NAME + "/" + Constraint.TAG_NAME, "add",
				"com.tippingpoint.database.Constraint");
		digester.addRule(Schema.TAG_NAME + "/" + Table.TAG_NAME + "/" + Constraint.TAG_NAME + "/" +
				ColumnDefinition.TAG_NAME, new ConstraintColumnRule());
		digester.addRule(Schema.TAG_NAME + "/" + Table.TAG_NAME + "/" + Constraint.TAG_NAME + "/" + Table.TAG_NAME,
				new ForeignTableRule());
		digester.addRule(Schema.TAG_NAME + "/" + Table.TAG_NAME + "/" + Constraint.TAG_NAME + "/" + Table.TAG_NAME +
				"/" + ColumnDefinition.TAG_NAME, new ForeignColumnRule());

		return digester;
	}
}

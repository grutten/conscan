package com.tippingpoint.database.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.Element;
import com.tippingpoint.database.Table;

/**
 * This class is used to convert table information to a JSON object.
 */
public class JsonTable {
	/** This member holds the table. */
	private final Table m_table;

	/**
	 * This method constructs a new instance for the given table.
	 */
	public JsonTable(final Table table) {
		m_table = table;
	}

	/**
	 * This method gets the object as a JSON object.
	 */
	public JSONObject get() {
		return get(true);
	}

	/**
	 * This method gets the object as a JSON object.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject get(final boolean bIncludeChildren) {
		final JSONObject objTable = new JSONObject();
		final JSONObject objTableContents = new JSONObject();

		// the table is just the contents
		objTable.put(Table.TAG_NAME, objTableContents);

		// the contents are all the attributes of the table
		objTableContents.put(Element.ATTRIBUTE_NAME, m_table.getName());

		if (bIncludeChildren) {
			// add all the columns to the array
			final JSONArray columns = new JSONArray();
			final Iterator<ColumnDefinition> iterColumns = m_table.getColumns();
			if (iterColumns != null && iterColumns.hasNext()) {
				while (iterColumns.hasNext()) {
					final JsonColumn jsonColumn = new JsonColumn(iterColumns.next());

					columns.add(jsonColumn);
				}
			}

			objTableContents.put(ColumnDefinition.TAG_NAME, columns);

			if (m_table.getPrimaryKey() != null) {
				final JsonConstraint jsonConstraint = new JsonConstraint(m_table.getPrimaryKey());

				objTableContents.put("primarykey", jsonConstraint);

				if (m_table.hasIdPrimaryKey()) {
					objTableContents.put("primarykeycolumn", m_table.getPrimaryKeyColumn().getName());
				}
			}

			if (m_table.getLogicalKey() != null) {
				final JsonConstraint jsonConstraint = new JsonConstraint(m_table.getLogicalKey());

				objTableContents.put("logicalkey", jsonConstraint);
			}

			final JSONArray constraints = new JSONArray();
			final Iterator<Constraint> iterConstraints = m_table.getConstraints();
			if (iterConstraints != null && iterConstraints.hasNext()) {
				while (iterConstraints.hasNext()) {
					final JsonConstraint jsonConstraint = new JsonConstraint(iterConstraints.next());

					constraints.add(jsonConstraint);
				}
			}

			objTableContents.put(Constraint.TAG_NAME, constraints);
		}

		return objTable;
	}

	/**
	 * This method persists the object to the specified writer.
	 * 
	 * @throws IOException
	 */
	public void write(final Writer writer) throws IOException {
		final JSONObject objTable = get();

		objTable.writeJSONString(writer);
	}
}

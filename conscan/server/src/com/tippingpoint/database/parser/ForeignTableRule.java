package com.tippingpoint.database.parser;

import org.xml.sax.Attributes;
import com.tippingpoint.database.ForeignKeyConstraint;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;

/**
 * This class is used to flag the foreign key table.
 */
public final class ForeignTableRule extends BaseRule {
	/**
	 * This method is called with the initial table tag. The table is identified and pushed onto the stack.
	 */
	@Override
	public void begin(final String namespace, final String name, final Attributes attributes) throws Exception {
		final Object objConstraint = getDigester().peek();
		if (objConstraint instanceof ForeignKeyConstraint) {
			final ForeignKeyConstraint constraint = (ForeignKeyConstraint)objConstraint;

			// get the schema to look up the parent table
			final Schema schema = (Schema)getDigester().peek(getDigester().getCount() - 1);

			final String strName = attributes.getValue(ATTRIBUTE_NAME);

			Table tableParent = schema.getTable(strName);
			if (tableParent == null) {
				final Table tableCurrent = (Table)getDigester().peek(1);
				if (strName.equals(tableCurrent.getName())) {
					tableParent = tableCurrent;
				}
			}

			constraint.setForeignTable(tableParent);

			getDigester().push(tableParent);
		}
	}

	/**
	 * This method removes the table pushed in the start of the tag.
	 */
	@Override
	public void end(final String namespace, final String name) throws Exception {
		getDigester().pop();
	}
}

package com.tippingpoint.database.parser;

import org.apache.commons.digester.Digester;
import org.xml.sax.Attributes;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.DatabaseElementException;
import com.tippingpoint.database.ForeignKey;
import com.tippingpoint.database.Table;

/**
 * This class is used to associate the parent columns of foreign keys to their corresponding child columns.
 */
public final class ForeignColumnRule extends BaseRule {
	/**
	 * This method is called with the initial table tag. The table is identified and pushed onto the stack.
	 * 
	 * @throws DatabaseElementException
	 */
	@Override
	public void begin(final String namespace, final String name, final Attributes attributes)
			throws DatabaseElementException {
		final Digester digester = getDigester();
		final Object objTable = digester.peek();
		if (objTable instanceof Table) {
			final Table tableParent = (Table)objTable;
			final String strName = attributes.getValue(ATTRIBUTE_NAME);
			final Column column = tableParent.getColumn(strName);
			if (column != null) {
				final ForeignKey foreignKey = (ForeignKey)digester.pop(ConstraintColumnRule.FOREIGN_KEY_STACK);
				if (foreignKey != null) {
					foreignKey.setParentColumn(column);
				}
				else {
					throw new DatabaseElementException(ATTRIBUTE_NAME, ForeignKey.class,
							"Foreign Key not available to set '" + column.getName() + "'");
				}
			}
			else {
				throw new DatabaseElementException(ATTRIBUTE_NAME, Column.class, "Column '" + strName +
						"' not found in table '" + tableParent.getName() + "'");
			}
		}
		else {
			throw new DatabaseElementException(null, Table.class, "Foreign key table not on the parsing stack.");
		}
	}
}

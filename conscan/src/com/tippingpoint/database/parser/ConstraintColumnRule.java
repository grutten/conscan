package com.tippingpoint.database.parser;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.digester.Digester;
import org.xml.sax.Attributes;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.DatabaseElementException;
import com.tippingpoint.database.ForeignKey;
import com.tippingpoint.database.ForeignKeyConstraint;
import com.tippingpoint.database.Table;

/**
 * This class is used to set the columns on a constraint.
 */
public final class ConstraintColumnRule extends BaseRule {
	static final String FOREIGN_KEY_STACK = "foreignkeys";

	/**
	 * This method sets the column on the constraint.
	 * 
	 * @throws DatabaseElementException
	 */
	@Override
	public void begin(final String namespace, final String name, final Attributes attributes)
			throws DatabaseElementException {
		final Object objConstraint = getDigester().peek();
		if (objConstraint instanceof com.tippingpoint.database.Constraint) {
			final Constraint constraint = (Constraint)objConstraint;

			Table table = constraint.getTable();
			if (table == null) {
				final Object objTable = getDigester().peek(1);
				if (objTable instanceof Table) {
					table = (Table)objTable;
				}
			}

			Column column = table.getColumn(attributes.getValue(ATTRIBUTE_NAME));
			if (column != null) {
				if (constraint instanceof ForeignKeyConstraint) {
					final ForeignKey foreignKey = new ForeignKey();

					foreignKey.setChildColumn(column);

					// use a separate stack to keep track of the foreign keys
					push(foreignKey);

					column = foreignKey;
				}

				constraint.addColumn(column);
			}
			else {
				throw new DatabaseElementException(ATTRIBUTE_NAME, constraint.getClass(),
						"Could not find column in parent table.");
			}
		}
	}

	/**
	 * This method pushes this foreign key onto the bottom of the stack. The stack should always look like: Keyn, ...,
	 * Key3, Key2, Key1
	 */
	private void push(final ForeignKey foreignKey) {
		final Digester digester = getDigester();

		final List<ForeignKey> listKeys = new ArrayList<ForeignKey>();
		listKeys.add(foreignKey);

		while (!digester.isEmpty(FOREIGN_KEY_STACK)) {
			final ForeignKey key = (ForeignKey)digester.pop(FOREIGN_KEY_STACK);
			listKeys.add(1, key);
		}

		// now push them back on the stack in order
		for (int nIndex = 0; nIndex < listKeys.size(); ++nIndex) {
			digester.push(FOREIGN_KEY_STACK, listKeys.get(nIndex));
		}
	}
}

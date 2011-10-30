package com.tippingpoint.database.parser;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.ConstraintFactory;

/**
 * This class constructs a constraint from the given constraint tag.
 */
public final class ConstraintRule extends Rule {
	/**
	 * This method constructs a constraint from the tag.
	 */
	@Override
	public void begin(final String namespace, final String name, final Attributes attributes) throws Exception {
		final String strType = attributes.getValue("type");
		final Constraint constraint = ConstraintFactory.getFactory().get(strType);
		getDigester().push(constraint);
	}

	/**
	 * This method removes the constraint constructed on the start of the tag.
	 */
	@Override
	public void end(final String namespace, final String name) throws Exception {
		getDigester().pop();
	}
}

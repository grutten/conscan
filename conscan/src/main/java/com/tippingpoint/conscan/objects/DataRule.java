package com.tippingpoint.conscan.objects;

import java.util.Map;

/**
 * This class applies a rule to a set of data.
 */
interface DataRule {
	/**
	 * This method applies the rule.
	 */
	void apply(final Map<String, FieldValue> mapValues);
}

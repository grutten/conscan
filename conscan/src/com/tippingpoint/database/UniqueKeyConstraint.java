package com.tippingpoint.database;

/**
 * This class represents a subclass of constraints that need to be unique.
 */
abstract class UniqueKeyConstraint extends Constraint {
	/**
	 * This method constructs a unique key based on the passed in type.
	 */
	public UniqueKeyConstraint(final String strType) {
		super(strType);
	}

	/**
	 * This method returns if the constraint represents a unique row.
	 */
	@Override
	public boolean isUnique() {
		return true; // by definition, these keys are unique
	}
}

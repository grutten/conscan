package com.tippingpoint.database;

/**
 * This class represents a logical key constraint. A logical key are the columns that should be unique in combination,
 * but an id is used instead for simplicity. Note that this does not represent any entity in the database.
 */
public class LogicalKeyConstraint extends UniqueKeyConstraint {
	static final String TYPE = "logical";

	/**
	 * This method constructs a new logical key constraint.
	 */
	public LogicalKeyConstraint() {
		super(TYPE);
	}

	/**
	 * This class is used to construct a constraint.
	 */
	public static class Factory extends ConstraintFactory {
		/**
		 * This method returns an instance of the constraint.
		 */
		@Override
		public Constraint get() {
			return new LogicalKeyConstraint();
		}

		/**
		 * This method returns the string representation of the constraint.
		 */
		@Override
		public String getType() {
			return TYPE;
		}
	}
}

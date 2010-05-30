package com.tippingpoint.database;

/**
 * PrimaryKeyConstraint
 */
public class PrimaryKeyConstraint extends UniqueKeyConstraint {
	static final String TYPE = "primary";

	/**
	 * This method constructs a new foreign key constraint.
	 * 
	 * @throws DatabaseElementException
	 */
	public PrimaryKeyConstraint() {
		super(TYPE);
	}

	/**
	 * This method determines if the constraints are equivalent.
	 */
	@Override
	public boolean equals(final Constraint constraint) {
		// be more accepting of name comparison since MySQL always names the primary key 'PRIMARY'
		return (getName().equals(constraint.getName()) || TYPE.equalsIgnoreCase(getName()) || TYPE
				.equalsIgnoreCase(constraint.getName())) &&
				getType().equals(constraint.getType()) && equals(m_columns, constraint.m_columns);
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
			return new PrimaryKeyConstraint();
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

package com.tippingpoint.database;

/**
 * This class is used to represent an index that is placed on a table.
 */
public class Index extends Constraint {
	static final String TYPE = "index";

	/** This member indicates if the index contains unique keys. */
	private boolean m_bUnique;

	/**
	 * This method constructs a new index (constraint).
	 * 
	 * @throws DatabaseElementException
	 */
	public Index() {
		super(TYPE);
	}

	/**
	 * @return Returns the unique.
	 */
	@Override
	public boolean isUnique() {
		return m_bUnique;
	}

	/**
	 * @param strUnique The unique to set.
	 */
	public void setUnique(final boolean strUnique) {
		m_bUnique = strUnique;
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
			return new Index();
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

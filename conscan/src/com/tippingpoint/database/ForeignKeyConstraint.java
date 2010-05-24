package com.tippingpoint.database;

/**
 * This class is used to hold the relationship between columns and their containing tables and other tables (i.e.
 * foreign keys).
 */
public final class ForeignKeyConstraint extends Constraint {
	public static final String TYPE = "foreign";

	/** This member holds a flag indicating if the foreign maps an id to and idref relationship. */
	private Boolean m_bMapsId;

	/** This member holds a reference to the table the column is a part of. */
	private Table m_foreignTable;

	/**
	 * This method constructs a new foreign key constraint.
	 */
	public ForeignKeyConstraint() {
		super(TYPE);
	}

	/**
	 * This method returns the table for which this foreign key is referencing.
	 */
	public Table getForeignTable() {
		return m_foreignTable;
	}

	/**
	 * This method returns if the column is part of this constraint.
	 * 
	 * @param column Column to check.
	 */
	@Override
	public boolean hasColumn(final Column column) {
		boolean bFound = false;

		for (int nIndex = 0; nIndex < m_columns.size() && !bFound; ++nIndex) {
			final ForeignKey key = (ForeignKey)m_columns.get(nIndex);

			bFound = column.equals(key.getChildColumn());
		}

		return bFound;
	}

	/**
	 * This method returns if this foreign key simply maps id values.
	 */
	public boolean mapsId() {
		if (m_bMapsId == null) {
			boolean bMapsId = false;
			if (m_columns.size() == 1) {
				final ForeignKey key = (ForeignKey)m_columns.get(0);

				bMapsId =
					key.getChildColumn().getType() instanceof ColumnTypeIdReference &&
							key.getParentColumn().getType() instanceof ColumnTypeId;
			}

			m_bMapsId = bMapsId ? Boolean.TRUE : Boolean.FALSE;
		}

		return m_bMapsId;
	}

	/**
	 * This method sets the table for which this foreign key is referencing.
	 */
	public void setForeignTable(final Table table) {
		m_foreignTable = table;

		// notify the other table a new key was added
		m_foreignTable.addReference(this);
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
			return new ForeignKeyConstraint();
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

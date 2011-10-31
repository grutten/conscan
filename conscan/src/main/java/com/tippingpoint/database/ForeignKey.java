package com.tippingpoint.database;

/**
 * This class is used to hold the foreign key relationships. Foreign keys are confusing because the relationship is two
 * ways, one from the table that contains the primary key (parent table) and from the table containing a reference to
 * the primary key (child table).
 */
public class ForeignKey implements Column {
	/** This member holds one of the columns involved in the join. */
	private Column m_childColumn;

	/** This member holds one of the columns involved in the join. */
	private Column m_parentColumn;

	/**
	 * This method determines if the keys are equivalent.
	 */
	public boolean equals(final ForeignKey key) {
		return equals(m_childColumn, key.m_childColumn) && equals(m_parentColumn, key.m_parentColumn);
	}

	/**
	 * This method determines if the keys are equivalent.
	 */
	@Override
	public boolean equals(final Object objValue) {
		return objValue instanceof ForeignKey && equals((ForeignKey)objValue);
	}

	/**
	 * @return Returns the childColumn.
	 */
	public Column getChildColumn() {
		return m_childColumn;
	}

	/**
	 * This method returns the fully qualified name of the child column.
	 * 
	 * @return Returns the fully qualified name.
	 */
	public String getFQName() {
		final StringBuilder strBuffer = new StringBuilder();
		if (getTable() != null) {
			strBuffer.append(getTable()).append('.');
		}
		strBuffer.append(getName());
		return strBuffer.toString();
	}

	/**
	 * This method returns the name of the child column.
	 * 
	 * @return Returns the name.
	 */
	public String getName() {
		return m_childColumn.getName();
	}

	/**
	 * @return Returns the parentColumn.
	 */
	public Column getParentColumn() {
		return m_parentColumn;
	}

	/**
	 * This method returns the table associated with the column.
	 */
	public Table getTable() {
		return m_childColumn.getTable();
	}

	/**
	 * This method returns the type of the child column.
	 * 
	 * @return Returns the type.
	 */
	public ColumnType getType() {
		return m_childColumn.getType();
	}

	/**
	 * @param strChildColumn The childColumn to set.
	 */
	public void setChildColumn(final Column childColumn) {
		m_childColumn = childColumn;
	}

	/**
	 * @param strParentColumn The parentColumn to set.
	 */
	public void setParentColumn(final Column parentColumn) {
		m_parentColumn = parentColumn;
	}

	/**
	 * This method displays a string representation of the foreign key.
	 */
	@Override
	public String toString() {
		return m_childColumn.getFQName() + " -> " + m_parentColumn.getFQName();
	}

	/**
	 * This member does a relatively simple check to see if the columns are equal by name.
	 */
	private boolean equals(final Column column1, final Column column2) {
		return column1.getTable().getName().equals(column2.getTable().getName()) &&
				column1.getName().equals(column2.getName());
	}
}

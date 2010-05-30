package com.tippingpoint.database;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is used to hold generic constraint information.
 */
public class Constraint extends Element {
	/** This member holds the columns involved in the constraint. */
	protected List<Column> m_columns = new ArrayList<Column>();

	/** This member holds a reference to the table the column is a part of. */
	private WeakReference<Table> m_parentTable;

	/** This member holds a textual indication of the type of constraint. */
	private String m_strType;

	/**
	 * This method constructs a new constraint of the given type.
	 * 
	 * @throws DatabaseElementException
	 */
	protected Constraint(final String strType) {
		setType(strType);
	}

	/**
	 * This method adds a column to the constraint.
	 */
	public void addColumn(final Column column) {
		m_columns.add(column);
	}

	/**
	 * This method determines if the constraints are equivalent.
	 */
	public boolean equals(final Constraint constraint) {
		return getName().equals(constraint.getName()) && equals(m_columns, constraint.m_columns);
	}

	/**
	 * This method determines if the constraints are equivalent.
	 */
	@Override
	public boolean equals(final Object objValue) {
		return objValue instanceof Constraint && equals((Constraint)objValue);
	}

	/**
	 * @return Returns the columns.
	 */
	public Iterator<Column> getColumns() {
		return m_columns.iterator();
	}

	/**
	 * This method returns the table that this constraint is a part of.
	 */
	public Table getTable() {
		return m_parentTable != null ? m_parentTable.get() : null;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return m_strType;
	}

	/**
	 * This method returns if the column is part of this constraint.
	 * 
	 * @param column Column to check.
	 */
	public boolean hasColumn(final Column column) {
		return m_columns.contains(column);
	}

	/**
	 * This method returns if the constraint represents a unique row.
	 */
	public boolean isUnique() {
		return false;
	}

	/**
	 * This method sets the table that this constraint is set on.
	 */
	public void setParentTable(final Table table) {
		m_parentTable = new WeakReference<Table>(table);
	}

	/**
	 * This method displays a string representation of the constraint.
	 */
	@Override
	public String toString() {
		return m_strType + ": " + getName() + " - " + m_columns;
	}

	/**
	 * This method determines of the column list are equivalent.
	 */
	protected boolean equals(final List<Column> listColumns1, final List<Column> listColumns2) {
		boolean bEquals = false;

		if (listColumns1 != null && listColumns2 != null) {
			if (listColumns1.size() == listColumns2.size()) {
				bEquals = true; // assume equal from here

				for (int nIndex = 0; nIndex < listColumns1.size() && bEquals; ++nIndex) {
					final Column column1 = listColumns1.get(nIndex);
					final Column column2 = listColumns2.get(nIndex);

					bEquals = column1.equals(column2);
				}
			}
		}

		return bEquals;
	}

	/**
	 * @param strType The type to set.
	 */
	void setType(final String strType) {
		m_strType = strType;
	}

	/**
	 * This class is used to construct a constraint.
	 */
	public static class ConstraintFactory {
		/**
		 * This method returns an instance of the constraint.
		 */
		public Constraint get() {
			return new PrimaryKeyConstraint();
		}

		/**
		 * This method returns the string representation of the constraint.
		 */
		public String getType() {
			return "constraint";
		}
	}
}

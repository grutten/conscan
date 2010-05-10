package com.tippingpoint.sql;

import java.util.ArrayList;
import java.util.List;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.Table;

/**
 * This class is use to generate the command to alter a table.
 */
public class SqlAlter extends TableBuilderCommand {
	/** This member holds constraints to be dropped in the table. */
	private final List<Constraint> m_listDroppedConstraints = new ArrayList<Constraint>();

	/** This member holds columns to be modified in the table. */
	private final List<ColumnDefinition> m_listModifyColumns = new ArrayList<ColumnDefinition>();

	/** This member holds constraints to be modified in the table. */
	private final List<Constraint> m_listModifyConstraints = new ArrayList<Constraint>();

	/** This member holds new columns to be added to the table. */
	private final List<ColumnDefinition> m_listNewColumns = new ArrayList<ColumnDefinition>();

	/** This member holds new constraints to be added to the table. */
	private final List<Constraint> m_listNewConstraints = new ArrayList<Constraint>();

	/**
	 * This method builds an alter table instance for the given table.
	 */
	public SqlAlter(final Table table) {
		super(table);
	}

	/**
	 * This method adds a new cConstraint to the list to be added to the table.
	 */
	public void add(final ColumnDefinition column) {
		if (m_table.getColumn(column.getName()) != null) {
			m_listModifyColumns.add(column);
		} else {
			m_listNewColumns.add(column);
		}
	}

	/**
	 * This method adds a new column to the list to be added to the table.
	 */
	public void add(final Constraint constraint) {
		if (m_table.getConstraint(constraint.getName()) != null) {
			m_listModifyConstraints.add(constraint);
		} else {
			m_listNewConstraints.add(constraint);
		}
	}

	/**
	 * This method adds a constraint to the list to be dropped from the table.
	 */
	public void drop(final Constraint constraint) {
		// only need to drop it if it exists
		if (m_table.getConstraint(constraint.getName()) != null) {
			m_listDroppedConstraints.add(constraint);
		}
	}

	public List<ColumnDefinition> getNewColumns() {
		return m_listNewColumns;
	}

	public List<Constraint> getNewConstraints() {
		return m_listNewConstraints;
	}

	public List<ColumnDefinition> getModifyColumns() {
		return m_listModifyColumns;
	}

	public List<Constraint> getModifyConstraints() {
		return m_listModifyConstraints;
	}

	public List<Constraint> getDroppedConstraints() {
		return m_listDroppedConstraints;
	}

}

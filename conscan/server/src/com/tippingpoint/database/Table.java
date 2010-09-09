package com.tippingpoint.database;

import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.tippingpoint.utilities.NameValuePair;
import com.tippingpoint.utilities.XmlUtilities;

/**
 * This class is used to hold the table representation.
 */
public class Table extends Element {
	public static final String TAG_NAME = "table";

	/** This member holds a flag indicating if the primary is simply an id. */
	private boolean m_bIdOnly;

	/** This member holds the id column of the table; if it has one. */
	private Column m_columnPrimaryKey;

	/** This member holds all the columns in the table. */
	private final Map<String, ColumnDefinition> m_columns = new LinkedHashMap<String, ColumnDefinition>();

	/** This member holds the logical key of the table, if specified. */
	private LogicalKeyConstraint m_constraintLogicalKey;

	/** This member holds the primary key of the table, if specified. */
	private PrimaryKeyConstraint m_constraintPrimaryKey;

	/** This member holds the constraints applied to the table. */
	private final List<Constraint> m_constraints = new ArrayList<Constraint>();

	/** This member holds the constraints to this table. */
	private final List<WeakReference<ForeignKeyConstraint>> m_listParentForeignKeys =
		new ArrayList<WeakReference<ForeignKeyConstraint>>();

	/**
	 * This method constructs a new table.
	 */
	public Table() {
	}

	/**
	 * This method constructs a new table with the given name.
	 */
	public Table(final Schema schema, final String strName) {
		super(strName);

		schema.addTable(this);
	}

	/**
	 * This method adds a column to the current table.
	 */
	public void add(final ColumnDefinition column) {
		m_columns.put(column.getName(), column);

		column.setParentTable(this);
	}

	/**
	 * This method adds a constraint to the table.
	 * 
	 * @throws DatabaseElementException
	 */
	public void add(final Constraint constraint) throws DatabaseElementException {
		constraint.setParentTable(this);

		if (constraint instanceof PrimaryKeyConstraint) {
			setPrimaryKey((PrimaryKeyConstraint)constraint);
		}
		else if (constraint instanceof LogicalKeyConstraint) {
			setLogicalKey((LogicalKeyConstraint)constraint);
		}
		else {
			m_constraints.add(constraint);
		}
	}

	/**
	 * This method removes the constraint from the table. Note that this does not change the database, it simply changes
	 * the definition.
	 * 
	 * @param constraint Constraint to be dropped.
	 */
	public void drop(final Constraint constraint) {
		if (m_constraintPrimaryKey != null && m_constraintPrimaryKey.equals(constraint)) {
			m_constraintPrimaryKey = null;
		}
		else if (m_constraintLogicalKey != null && m_constraintLogicalKey.equals(constraint)) {
			m_constraintLogicalKey = null;
		}
		else {
			for (int nIndex = 0; nIndex < m_constraints.size(); ++nIndex) {
				final Constraint constraintCurrent = m_constraints.get(nIndex);
				if (constraintCurrent.equals(constraint)) {
					m_constraints.remove(nIndex);
					break;
				}
			}
		}
	}

	/**
	 * This method returns a named column in the table.
	 */
	public ColumnDefinition getColumn(final String strName) {
		return m_columns.get(strName);
	}

	/**
	 * This method returns the number of columns currently found in the table.
	 */
	public int getColumnCount() {
		return m_columns.size();
	}

	/**
	 * @return Returns the columns.
	 */
	public Iterator<ColumnDefinition> getColumns() {
		return m_columns.values().iterator();
	}

	/**
	 * This method returns the named constraint.
	 * 
	 * @param strName String containing the name of the constraint to search.
	 */
	public Constraint getConstraint(final String strName) {
		Constraint constraint = null;

		if (m_constraintPrimaryKey != null && strName.equals(m_constraintPrimaryKey.getName())) {
			constraint = m_constraintPrimaryKey;
		}
		else if (m_constraintLogicalKey != null && strName.equals(m_constraintLogicalKey.getName())) {
			constraint = m_constraintLogicalKey;
		}
		else {
			for (int nIndex = 0; nIndex < m_constraints.size() && constraint == null; ++nIndex) {
				final Constraint constraintCurrent = m_constraints.get(nIndex);
				if (strName.equals(constraintCurrent.getName())) {
					constraint = constraintCurrent;
				}
			}
		}

		return constraint;
	}

	/**
	 * This method returns a list containing all of the constraints for the table (including primary and logical keys).
	 */
	public List<Constraint> getConstraintList() {
		final List<Constraint> listConstraints = new ArrayList<Constraint>(m_constraints);

		if (m_constraintLogicalKey != null) {
			listConstraints.add(0, m_constraintLogicalKey);
		}

		if (m_constraintPrimaryKey != null) {
			listConstraints.add(0, m_constraintPrimaryKey);
		}

		return listConstraints;
	}

	/**
	 * @return Returns the constraints.
	 */
	public Iterator<Constraint> getConstraints() {
		return m_constraints.iterator();
	}

	/**
	 * This method returns the foreign key associated with the passed on parent column.
	 * 
	 * @param columnParentId Column referring to the parent table.
	 */
	public ForeignKey getForeignKey(final Column columnParentId) {
		ForeignKey foundForeignKey = null;

		final Iterator<Constraint> iterConstraints = getConstraints();
		if (iterConstraints != null && iterConstraints.hasNext()) {
			while (iterConstraints.hasNext() && foundForeignKey == null) {
				final Constraint constraint = iterConstraints.next();
				if (constraint instanceof ForeignKeyConstraint) {
					final ForeignKeyConstraint foreignKeyConstraint = (ForeignKeyConstraint)constraint;
					final Iterator<Column> iterForeignKeys = foreignKeyConstraint.getColumns();
					if (iterForeignKeys != null && iterForeignKeys.hasNext()) {
						while (iterForeignKeys.hasNext() && foundForeignKey == null) {
							final ForeignKey foreignKey = (ForeignKey)iterForeignKeys.next();

							if (columnParentId.equals(foreignKey.getParentColumn())) {
								foundForeignKey = foreignKey;
							}
						}
					}
				}
			}
		}

		return foundForeignKey;
	}

	/**
	 * This member returns the logical key for the table.
	 */
	public LogicalKeyConstraint getLogicalKey() {
		return m_constraintLogicalKey;
	}

	/**
	 * This member returns the primary key for the table.
	 */
	public PrimaryKeyConstraint getPrimaryKey() {
		return m_constraintPrimaryKey;
	}

	/**
	 * This method returns the id column of the table. This will only return a column if hasIdPrimaryKey() returns true.
	 */
	public Column getPrimaryKeyColumn() {
		return m_columnPrimaryKey;
	}

	/**
	 * This method returns the foreign keys that are referencing this table.
	 */
	public List<ForeignKeyConstraint> getReferences() {
		final List<ForeignKeyConstraint> listReferences =
			new ArrayList<ForeignKeyConstraint>(m_listParentForeignKeys.size());

		for (int nIndex = 0; nIndex < m_listParentForeignKeys.size(); ++nIndex) {
			listReferences.add(m_listParentForeignKeys.get(nIndex).get());
		}

		return listReferences;
	}

	/**
	 * This method returns if this has a primary key of an id only.
	 */
	public boolean hasIdPrimaryKey() {
		return m_bIdOnly;
	}

	/**
	 * This method returns if a primary key has been specified for the table.
	 */
	public boolean hasPrimaryKey() {
		return m_constraintPrimaryKey != null;
	}

	/**
	 * This method dumps the element in XML to the writer.
	 * 
	 * @param writer Writer where the table XML is to be written.
	 * @throws IOException
	 */
	public void writeXml(final Writer writer) throws IOException {
		writeXml(writer, true);
	}

	/**
	 * This method dumps the element in XML to the writer.
	 * 
	 * @param writer Writer where the table XML is to be written.
	 * @throws IOException
	 */
	public void writeXml(final Writer writer, final boolean bIncludeChildren) throws IOException {
		if (bIncludeChildren) {
			writer.append(XmlUtilities.open(TAG_NAME, new NameValuePair(Element.ATTRIBUTE_NAME, getName())));

			final Iterator<ColumnDefinition> iterColumns = getColumns();
			if (iterColumns != null && iterColumns.hasNext()) {
				while (iterColumns.hasNext()) {
					iterColumns.next().writeXml(writer);
				}
			}

			if (m_constraintPrimaryKey != null) {
				m_constraintPrimaryKey.writeXml(writer);
			}

			if (m_constraintLogicalKey != null) {
				m_constraintLogicalKey.writeXml(writer);
			}

			final Iterator<Constraint> iterConstraints = getConstraints();
			if (iterConstraints != null && iterConstraints.hasNext()) {
				while (iterConstraints.hasNext()) {
					iterConstraints.next().writeXml(writer);
				}
			}

			writer.append(XmlUtilities.close(TAG_NAME));
		}
		else {
			writer.append(XmlUtilities.tag(TAG_NAME, new NameValuePair(Element.ATTRIBUTE_NAME, getName())));
		}
	}

	/**
	 * This method adds a reference to this table by a foreign key.
	 */
	void addReference(final ForeignKeyConstraint key) {
		m_listParentForeignKeys.add(new WeakReference<ForeignKeyConstraint>(key));
	}

	/**
	 * This member sets the logical key for the table.
	 * 
	 * @throws DatabaseElementException
	 */
	private void setLogicalKey(final LogicalKeyConstraint constraintLogicalKey) throws DatabaseElementException {
		if (m_constraintLogicalKey != null) {
			throw new DatabaseElementException(constraintLogicalKey.getName(), Table.class,
					"logical key has been set previously");
		}

		m_constraintLogicalKey = constraintLogicalKey;
	}

	/**
	 * This member sets the primary key for the table.
	 * 
	 * @throws DatabaseElementException
	 */
	private void setPrimaryKey(final PrimaryKeyConstraint constraintPrimaryKey) throws DatabaseElementException {
		if (m_constraintPrimaryKey != null) {
			throw new DatabaseElementException(constraintPrimaryKey.getName(), Table.class,
					"primary key has been set previously");
		}

		m_constraintPrimaryKey = constraintPrimaryKey;

		final Iterator<Column> iterColumns = m_constraintPrimaryKey.getColumns();
		if (iterColumns.hasNext()) {
			final Column column = iterColumns.next();
			m_bIdOnly = column.getType() instanceof ColumnTypeId && !iterColumns.hasNext();
			if (m_bIdOnly) {
				m_columnPrimaryKey = column;
			}
		}
	}
}

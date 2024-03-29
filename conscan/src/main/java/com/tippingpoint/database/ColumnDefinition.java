package com.tippingpoint.database;

import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.ObjectUtils;
import com.tippingpoint.utilities.NameValuePair;
import com.tippingpoint.utilities.XmlUtilities;

/**
 * This class holds the information that defines a column.
 */
public class ColumnDefinition extends Element implements Column {
	public static final String ATTRIBUTE_LENGTH = "length";
	public static final String ATTRIBUTE_REQUIRED = "required";
	public static final String ATTRIBUTE_TYPE = "type";
	public static final String TAG_NAME = "column";

	/** This member holds the requirement indicator. */
	private boolean m_bRequired;

	/** This member holds the length of the column. */
	private int m_nLength = -1;

	/** This member holds the default value of the column. */
	private Object m_objDefault;

	/** This member holds a reference to the table the column is a part of. */
	private WeakReference<Table> m_parentTable;

	/** This member holds the type of the column. */
	private ColumnType m_type;

	/**
	 * This method constructs a new column.
	 */
	public ColumnDefinition() {
	}

	/**
	 * This method constructs a new column for the specified table.
	 */
	public ColumnDefinition(final Table table, final String strName, final ColumnType type) {
		super(strName);

		setParentTable(table);
		setType(type);

		// add this column to the table
		table.add(this);
	}

	/**
	 * This method determines if the columns are equivalent.
	 */
	public boolean equals(final ColumnDefinition column) {
		return getName().equals(column.getName()) && m_type.equals(column.getType()) && m_nLength == column.m_nLength &&
				m_bRequired == column.m_bRequired && ObjectUtils.equals(m_objDefault, column.m_objDefault);
	}

	/**
	 * This method determines if the columns are equivalent.
	 */
	@Override
	public boolean equals(final Object objValue) {
		return objValue instanceof ColumnDefinition && equals((ColumnDefinition)objValue);
	}

	/**
	 * @return Returns the default.
	 */
	public Object getDefault() {
		return m_objDefault;
	}

	/**
	 * This method returns the fully qualified name of the child column.
	 * 
	 * @return Returns the fully qualified name.
	 */
	@Override
	public String getFQName() {
		final StringBuilder strBuffer = new StringBuilder();
		if (getTable() != null) {
			strBuffer.append(getTable()).append('.');
		}
		strBuffer.append(getName());
		return strBuffer.toString();
	}

	/**
	 * @return Returns the length.
	 */
	public int getLength() {
		int nLength = m_nLength;

		if (m_type.hasLength() && m_type.isLengthSetByType()) {
			nLength = m_type.getLength();
		}

		return nLength;
	}

	/**
	 * This method returns the table for which this column is a part of.
	 */
	@Override
	public Table getTable() {
		return m_parentTable != null ? m_parentTable.get() : null;
	}

	/**
	 * @return Returns the type.
	 */
	@Override
	public ColumnType getType() {
		return m_type;
	}

	/**
	 * @return Returns the required.
	 */
	public boolean isRequired() {
		return m_bRequired;
	}

	/**
	 * This method returns if the type dictates if the value is required when the column is required. Values not
	 * required when the column type indicates that the column is specified are values generated by the database (i.e.
	 * id columns).
	 */
	public boolean isValueRequired() {
		return getType().isValueRequired(m_bRequired);
	}

	/**
	 * @param objDefault The default to set.
	 */
	public void setDefault(final Object objDefault) {
		m_objDefault = objDefault;
	}

	/**
	 * @param nLength The length to set.
	 */
	public void setLength(final int nLength) {
		m_nLength = nLength;
	}

	/**
	 * This method sets the parent table of this column.
	 */
	public void setParentTable(final Table table) {
		m_parentTable = new WeakReference<Table>(table);
	}

	/**
	 * This method sets the requirement of the column
	 */
	public void setRequired(final boolean bRequired) {
		m_bRequired = bRequired;
	}

	/**
	 * This method sets the column type based on the passed in value.
	 */
	public void setType(final ColumnType type) {
		m_type = type;

		m_type.setRestrictions(this);
	}

	/**
	 * This method returns the full name of the column (including table name).
	 */
	@Override
	public String toString() {
		return getFQName();
	}

	/**
	 * This method dumps the element in XML to the writer.
	 * 
	 * @param writer Writer where the table XML is to be written.
	 * @throws IOException
	 */
	public void writeXml(final Writer writer) throws IOException {
		// <column name="firstname" type="string" length="100"/>
		final List<NameValuePair> listAttributes = new ArrayList<NameValuePair>();

		listAttributes.add(new NameValuePair(Element.ATTRIBUTE_NAME, getName()));
		listAttributes.add(new NameValuePair(ATTRIBUTE_TYPE, getType().getType()));

		if (getType().hasLength()) {
			listAttributes.add(new NameValuePair(ATTRIBUTE_LENGTH, Integer.toOctalString(getLength())));
		}

		if (isRequired()) {
			listAttributes.add(new NameValuePair(ATTRIBUTE_REQUIRED, Boolean.TRUE.toString()));
		}

		writer.append(XmlUtilities.tag(TAG_NAME, listAttributes));
	}
}

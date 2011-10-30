package com.tippingpoint.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is the base class for all column types for the system.
 */
public abstract class ColumnType {
	/** This member holds the string version of the column type. */
	private final String m_strType;

	/**
	 * This method constructs a new column for the type specified.
	 * 
	 * @param strType String containing the string version of the column type.
	 */
	protected ColumnType(final String strType) {
		m_strType = strType;
	}

	/**
	 * This method returns the JDBC Type, used for setting null.
	 */
	public abstract int getJdbcType();

	/**
	 * This returns the length associated with the type.
	 */
	public int getLength() {
		// default is to associated the length with the particular column
		return -1;
	}

	/**
	 * This method returns an object that represents the value found in the result set.
	 * 
	 * @throws SQLException
	 */
	public abstract Object getResult(ResultSet rs, int nIndex) throws SQLException;

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return m_strType;
	}

	/**
	 * This method is used to indicate that a length is associated with this column type.
	 */
	public boolean hasLength() {
		return false; // default is to assume no length
	}

	/**
	 * This method returns if the value is derived based on the database.
	 */
	public boolean idDerived() {
		return false; // default is that the column type is not derived
	}

	/**
	 * This method returns if the type defines the length of the field.
	 */
	public boolean isLengthSetByType() {
		// default is to associated the length with the particular column
		return false;
	}

	/**
	 * This method returns if the type dictates if the value is required when the column is required. Values not
	 * required when the column type indicates that the column is specified are values generated by the database (i.e.
	 * id columns).
	 */
	public boolean isValueRequired(final boolean bColumnRequired) {
		return bColumnRequired; // default is to return the column requirement
	}

	/**
	 * This method gives a chance for the type to set any restrictions on the column type.
	 * 
	 * @param columnDefinition ColumnDefinition being defined by this type.
	 */
	public void setRestrictions(final ColumnDefinition columnDefinition) {
		// by default, there is nothing to do
	}

	/**
	 * This method prints the column type as a string.
	 */
	@Override
	public String toString() {
		return m_strType;
	}
}

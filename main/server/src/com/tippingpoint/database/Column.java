package com.tippingpoint.database;

/**
 * This interface is used to reference a column. Columns can be either explicit columns or aliased columns.
 */
public interface Column {
	/**
	 * This method returns the fully qualified name of the column.
	 * 
	 * @return Returns the name.
	 */
	String getFQName();

	/**
	 * This method returns the name of the column.
	 * 
	 * @return Returns the name.
	 */
	String getName();

	/**
	 * This method returns the table associated with the column.
	 */
	Table getTable();

	/**
	 * This method returns the type of the column.
	 * 
	 * @return Returns the type.
	 */
	ColumnType getType();
}

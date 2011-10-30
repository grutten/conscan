package com.tippingpoint.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This interface defines what an ID factory will define.
 */
public interface IdFactory {
	/**
	 * This method returns the database type for references.
	 */
	String getDatabaseReferenceType();

	/**
	 * This method returns the JDBC type associate with the type of ID generated.
	 */
	int getJdbcType();

	/**
	 * This method returns the length of the column needed to store the id.
	 */
	int getLength();

	/**
	 * This method returns a new value for the ID.
	 */
	Id getNewValue();

	/**
	 * This method returns the value from the result set as an ID.
	 * 
	 * @param rs Result set containing the results.
	 * @param intIndex Integer corresponding to the index of the column in the result set.
	 * @throws SQLException
	 */
	Id getValue(ResultSet rs, Integer intIndex) throws SQLException;

	/**
	 * This method is used to indicate that a length is associated with this column type.
	 */
	boolean hasLength();

	/**
	 * This method returns if the value is derived based on the database.
	 */
	boolean idDerived();

	/**
	 * This method returns if the type dictates if the value is required when the column is required. Values not
	 * required when the column type indicates that the column is specified are values generated by the database (i.e.
	 * auto increment id columns).
	 */
	boolean isValueRequired(boolean bColumnRequired);
}

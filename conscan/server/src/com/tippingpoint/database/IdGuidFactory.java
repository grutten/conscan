package com.tippingpoint.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 * This class represents an ID in the database that is a GUID.
 */
public class IdGuidFactory implements IdFactory {
	/**
	 * This method returns the database type for references.
	 */
	public String getDatabaseReferenceType() {
		return "CHAR";
	}

	/**
	 * This method returns the JDBC type associate with the type of ID generated.
	 */
	public int getJdbcType() {
		return Types.CHAR;
	}

	/**
	 * This method returns the length of the column needed to store the id.
	 */
	public int getLength() {
		return 36;
	}

	/**
	 * This method returns a new value for the ID.
	 */
	public Id getNewValue() {
		return new Id(UUID.randomUUID().toString());
	}

	/**
	 * This method returns the value from the result set as an ID.
	 * 
	 * @param rs Result set containing the results.
	 * @param intIndex Integer corresponding to the index of the column in the result set.
	 * @throws SQLException
	 */
	public Id getValue(final ResultSet rs, final Integer intIndex) throws SQLException {
		Id id = null;
		final String strId = rs.getString(intIndex);

		if (!rs.wasNull()) {
			id = new Id(strId);
		}

		return id;
	}

	/**
	 * This method is used to indicate that a length is associated with this column type.
	 */
	public boolean hasLength() {
		return true;
	}

	/**
	 * This method returns if the value is derived based on the database.
	 */
	public boolean idDerived() {
		return false;
	}

	/**
	 * This method returns if the type dictates if the value is required when the column is required. Values not
	 * required when the column type indicates that the column is specified are values generated by the database (i.e.
	 * auto increment id columns).
	 */
	public boolean isValueRequired(final boolean bColumnRequired) {
		return true;
	}
}
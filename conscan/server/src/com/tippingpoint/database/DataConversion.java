package com.tippingpoint.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.tippingpoint.sql.Command;

/**
 * This class is used to map and convert objects to a certain form for use with SQL statements.
 */
public final class DataConversion {
	/** This member holds the conversion map. */
	private static Map<Class<? extends ColumnType>, ColumnTypeConversion> m_conversions =
		new HashMap<Class<? extends ColumnType>, ColumnTypeConversion>();

	/**
	 * This method converts the object to an object acceptable to be placed as a parameter in a SQL statement. Default
	 * conversions are performed if necessary.
	 */
	public Object convertToSqlObject(final ColumnType type, final Object objValue) {
		Object objReturnValue = objValue;
		final ColumnTypeConversion conversion = getConversion(type);

		if (conversion != null) {
			objReturnValue = conversion.convertToSqlObject(objValue);
		}

		return objReturnValue;
	}

	/**
	 * This method converts the object to a string acceptable to be placed in a SQL statement. Default conversions are
	 * performed if necessary. Additionally, glitches are added, if necessary. This method should be used
	 * conservatively, since parameterized SQL statements are preferable.
	 */
	public String convertToSqlString(final ColumnType type, final Object objValue) {
		String strValue = null;
		final ColumnTypeConversion conversion = getConversion(type);

		if (conversion != null) {
			strValue = conversion.convertToSqlString(objValue);
		}
		else if (objValue != null) {
			strValue = objValue.toString();
		}

		return strValue;
	}

	/**
	 * This method returns the value of the item in the results set as a type native to the column.
	 * 
	 * @param type ColumnType instance corresponding to the column in the result set.
	 * @param rs Result set containing the results.
	 * @param intIndex Integer corresponding to the index of the column in the result set.
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	public Object getObject(final ColumnType type, final ResultSet rs, final Integer intIndex) throws SQLException,
			DatabaseException {
		Object objValue = null;

		final ColumnTypeConversion conversion = getConversion(type);
		if (conversion != null) {
			objValue = conversion.convertToObject(rs, intIndex);
		}
		else {
			throw new DatabaseException("Can not convert column '" + intIndex + "' of type " + type.getType());
		}

		return objValue;
	}

	/**
	 * This method returns a conversion class for the type passed in or any of it's super classes.
	 */
	private ColumnTypeConversion getConversion(final ColumnType type) {
		Class<?> clsType = type.getClass();
		ColumnTypeConversion conversion = m_conversions.get(clsType);

		// search up the hierarchy until a converter is found or the base class is found
		while (conversion == null && !clsType.equals(ColumnType.class)) {
			// look at the super type
			clsType = clsType.getSuperclass();

			// see if there is a converter for the current column's type
			conversion = m_conversions.get(clsType);
		}

		return conversion;
	}

	static {
		m_conversions.put(ColumnTypeString.class, new ColumnTypeStringConversion());
		m_conversions.put(ColumnTypeIdBase.class, new ColumnTypeIdConversion());
		m_conversions.put(ColumnTypeDate.class, new ColumnTypeDateConversion());
		m_conversions.put(ColumnTypeInteger.class, new ColumnTypeIntegerConversion());
		m_conversions.put(ColumnTypeBoolean.class, new ColumnTypeBooleanConversion());
	}

	/**
	 * This class is a base class for converting type classes.
	 */
	public abstract static class ColumnTypeConversion {
		/**
		 * This method reads the entry in the result set and returns it as a type native to the column type.
		 * 
		 * @param rs Result set containing the results.
		 * @param intIndex Integer corresponding to the index of the column in the result set.
		 * @throws SQLException
		 */
		public Object convertToObject(final ResultSet rs, final Integer intIndex) throws SQLException {
			// default action is to let JDBC do it
			return rs.getObject(intIndex);
		}

		/**
		 * This method converts the object to an object acceptable to be placed as a parameter in a SQL statement.
		 */
		public Object convertToSqlObject(final Object objValue) {
			return objValue; // default is to return the object
		}

		/**
		 * This method converts the object to a string acceptable to be placed in a SQL statement. Default conversions
		 * are performed if necessary. Additionally, glitches are added, if necessary. This method should be used
		 * conservatively, since parameterized SQL statements are preferable.
		 */
		public String convertToSqlString(final Object objValue) {
			String strValue = Command.SQL_NULL;

			if (objValue != null) {
				strValue = objValue.toString();
			}

			return strValue;
		}
	}

	/**
	 * This class is used to convert boolean type conversions.
	 */
	private static class ColumnTypeBooleanConversion extends ColumnTypeConversion {
		/**
		 * This method reads the entry in the result set and returns it as a type native to the column type.
		 * 
		 * @param rs Result set containing the results.
		 * @param intIndex Integer corresponding to the index of the column in the result set.
		 * @throws SQLException
		 */
		@Override
		public Object convertToObject(final ResultSet rs, final Integer intIndex) throws SQLException {
			Boolean result = Boolean.FALSE;
			final int nValue = rs.getInt(intIndex);

			if (!rs.wasNull() && nValue != 0) {
				result = Boolean.TRUE;
			}

			return result;
		}

		/**
		 * This method converts the object to an object acceptable to be placed as a parameter in a SQL statement.
		 */
		@Override
		public Object convertToSqlObject(final Object objValue) {
			Object objReturnValue = objValue;

			if (objValue instanceof Boolean) {
				objReturnValue = new Integer(Boolean.TRUE.equals(objValue) ? 1 : 0);
			} else if (objValue instanceof String) {
				objReturnValue = new Integer("true".equalsIgnoreCase(objValue.toString()) ? 1 : 0);
			}

			return objReturnValue;
		}

		/**
		 * This method converts the object to a string acceptable to be placed in a SQL statement. Default conversions
		 * are performed if necessary. Additionally, glitches are added, if necessary. This method should be used
		 * conservatively, since parameterized SQL statements are preferable.
		 */
		@Override
		public String convertToSqlString(final Object objValue) {
			String strValue = Command.SQL_NULL;

			if (objValue != null) {
				strValue = objValue.toString();
			}

			return strValue;
		}
	}

	/**
	 * This class is used to convert Date type conversions.
	 */
	private static class ColumnTypeDateConversion extends ColumnTypeConversion {
		/**
		 * This method reads the entry in the result set and returns it as a type native to the column type.
		 * 
		 * @param rs Result set containing the results.
		 * @param intIndex Integer corresponding to the index of the column in the result set.
		 * @throws SQLException
		 */
		@Override
		public Object convertToObject(final ResultSet rs, final Integer intIndex) throws SQLException {
			Date dateValue = null;
			final java.sql.Date dtValue = rs.getDate(intIndex);
			if (!rs.wasNull()) {
				dateValue = new Date(dtValue.getTime());
			}

			return dateValue;
		}

		/**
		 * This method converts the object to an object acceptable to be placed as a parameter in a SQL statement.
		 */
		@Override
		public Object convertToSqlObject(final Object objValue) {
			Object objReturnValue = null;

			if (objValue instanceof Date) {
				objReturnValue = new Timestamp(((Date)objValue).getTime());
			}

			return objReturnValue;
		}

		/**
		 * This method converts the object to a string acceptable to be placed in a SQL statement. Default conversions
		 * are performed if necessary. Additionally, glitches are added, if necessary. This method should be used
		 * conservatively, since parameterized SQL statements are preferable.
		 */
		@Override
		public String convertToSqlString(final Object objValue) {
			throw new IllegalStateException("Can not genericly specify a date default.");
		}
	}

	/**
	 * This class is used to convert Id type conversions.
	 */
	private static class ColumnTypeIdConversion extends ColumnTypeConversion {
		/**
		 * This method reads the entry in the result set and returns it as a type native to the column type.
		 * 
		 * @param rs Result set containing the results.
		 * @param intIndex Integer corresponding to the index of the column in the result set.
		 * @throws SQLException
		 */
		@Override
		public Object convertToObject(final ResultSet rs, final Integer intIndex) throws SQLException {
			Id id = null;
			final int nId = rs.getInt(intIndex);

			if (!rs.wasNull()) {
				id = new Id(nId);
			}

			return id;
		}

		/**
		 * This method converts the object to an object acceptable to be placed as a parameter in a SQL statement.
		 */
		@Override
		public Object convertToSqlObject(final Object objValue) {
			Object objReturnValue = objValue;

			if (objValue instanceof Id) {
				objReturnValue = ((Id)objValue).getValue();
			}

			return objReturnValue;
		}

		/**
		 * This method converts the object to a string acceptable to be placed in a SQL statement. Default conversions
		 * are performed if necessary. Additionally, glitches are added, if necessary. This method should be used
		 * conservatively, since parameterized SQL statements are preferable.
		 */
		@Override
		public String convertToSqlString(final Object objValue) {
			String strValue = Command.SQL_NULL;

			if (objValue != null) {
				strValue = objValue.toString();
			}

			return strValue;
		}
	}

	/**
	 * This class is used to convert Id type conversions.
	 */
	private static class ColumnTypeIntegerConversion extends ColumnTypeConversion {
		/**
		 * This method reads the entry in the result set and returns it as a type native to the column type.
		 * 
		 * @param rs Result set containing the results.
		 * @param intIndex Integer corresponding to the index of the column in the result set.
		 * @throws SQLException
		 */
		@Override
		public Object convertToObject(final ResultSet rs, final Integer intIndex) throws SQLException {
			Integer result = null;
			final int nId = rs.getInt(intIndex);

			if (!rs.wasNull()) {
				result = new Integer(nId);
			}

			return result;
		}

		/**
		 * This method converts the object to an object acceptable to be placed as a parameter in a SQL statement.
		 */
		@Override
		public Object convertToSqlObject(final Object objValue) {
			Object objReturnValue = objValue;

			if (objValue instanceof String) {
				objReturnValue = new Integer((String)objValue);
			}

			return objReturnValue;
		}

		/**
		 * This method converts the object to a string acceptable to be placed in a SQL statement. Default conversions
		 * are performed if necessary. Additionally, glitches are added, if necessary. This method should be used
		 * conservatively, since parameterized SQL statements are preferable.
		 */
		@Override
		public String convertToSqlString(final Object objValue) {
			String strValue = Command.SQL_NULL;

			if (objValue != null) {
				strValue = objValue.toString();
			}

			return strValue;
		}
	}

	/**
	 * This class is used to convert string type conversions.
	 */
	private static class ColumnTypeStringConversion extends ColumnTypeConversion {
		/**
		 * This method reads the entry in the result set and returns it as a type native to the column type.
		 * 
		 * @param rs Result set containing the results.
		 * @param intIndex Integer corresponding to the index of the column in the result set.
		 * @throws SQLException
		 */
		@Override
		public Object convertToObject(final ResultSet rs, final Integer intIndex) throws SQLException {
			return rs.getString(intIndex);
		}

		/**
		 * This method converts the object to a string acceptable to be placed in a SQL statement. Default conversions
		 * are performed if necessary. Additionally, glitches are added, if necessary. This method should be used
		 * conservatively, since parameterized SQL statements are preferable.
		 */
		@Override
		public String convertToSqlString(final Object objValue) {
			String strValue = Command.SQL_NULL;

			if (objValue != null) {
				final StringBuilder strBuffer = new StringBuilder();

				strBuffer.append('\'').append(objValue).append('\'');

				strValue = strBuffer.toString();
			}

			return strValue;
		}
	}
}

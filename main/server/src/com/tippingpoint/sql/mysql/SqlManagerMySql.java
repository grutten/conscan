package com.tippingpoint.sql.mysql;

import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.ColumnTypeBoolean;
import com.tippingpoint.database.ColumnTypeId;
import com.tippingpoint.database.IdFactory;
import com.tippingpoint.sql.SqlAlter;
import com.tippingpoint.sql.base.SqlManager;

public class SqlManagerMySql extends SqlManager {
	/**
	 * This method constructs a new MySQL builder.
	 */
	public SqlManagerMySql(final IdFactory idFactory) {
		super(idFactory);

		register(new IdColumnTypeConverter(idFactory));
		register(new BooleanColumnTypeConverter());

		register(new SqlAlterExecutionFactory(), SqlAlter.class);

		setSqlSchema(new SqlSchemaMySql(this));
	}

	/**
	 * This method returns the SQL used to query the database for the definitions of columns of the named table. The
	 * columns returned should be:
	 * <ul>
	 * <li>COLUMN_NAME - String containing the name of the column</li>
	 * <li>COLUMN_DEFAULT - Default value of the column</li>
	 * <li>IS_NULLABLE - String containing a 'YES' or 'NO' indicating if the column is nullable</li>
	 * <li>ID_COLUMN - int indicating if the column is an identity column</li>
	 * <li>DATA_TYPE - String containing the type of column</li>
	 * <li>CHARACTER_MAXIMUM_LENGTH - int containing the length of the text fields</li>
	 * </ul>
	 * 
	 * @param strDatabaseName String containing the name of the database.
	 * @param strTableName String containing the name of the table.
	 */
	@Override
	public String getTableDefinitionSql(final String strDatabaseName, final String strTableName) {
		return "SELECT COLUMN_NAME, COLUMN_DEFAULT, IS_NULLABLE, IF(EXTRA = 'auto_increment', 1, 0) ID_COLUMN, " +
				"DATA_TYPE, CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" +
				strDatabaseName + "' AND TABLE_NAME = '" + strTableName + "' " + "ORDER BY ORDINAL_POSITION";
	}

	/**
	 * This class returns the string version of the type for booleans.
	 */
	protected static class BooleanColumnTypeConverter extends ColumnTypeConverter {
		/**
		 * This method creates a new type converter.
		 */
		public BooleanColumnTypeConverter() {
			super(ColumnTypeBoolean.TYPE);
		}

		/**
		 * This method returns the string version of the type.
		 */
		@Override
		public String get(final ColumnDefinition column) {
			final StringBuilder strBuffer = new StringBuilder();

			strBuffer.append("tinyint ");
			strBuffer.append(BOOLEAN_CHECK_PREFIX);
			strBuffer.append(column.getTable().getName().toUpperCase());
			strBuffer.append("_");
			strBuffer.append(column.getName().toUpperCase());
			strBuffer.append(" CHECK (");
			strBuffer.append(column.getName());
			strBuffer.append(" = 1 OR ");
			strBuffer.append(column.getName());
			strBuffer.append(" = 0)");

			// return strBuffer.toString();
			return "BOOLEAN";
		}

		/**
		 * This method returns the database type associated with this converter.
		 */
		@Override
		public String getDatabaseType() {
			return "tinyint";
		}
	}

	/**
	 * This class returns the string version of the type for ids.
	 */
	protected static class IdColumnTypeConverter extends ColumnTypeConverter {
		/** This member holds the ID factory used to define ID fields. */
		private final IdFactory m_idFactory;

		/**
		 * This method creates a new type converter.
		 */
		public IdColumnTypeConverter(final IdFactory idFactory) {
			super(ColumnTypeId.TYPE);

			m_idFactory = idFactory;
		}

		/**
		 * This method returns the string version of the type.
		 */
		@Override
		public String get(final ColumnDefinition column) {
			final StringBuilder strBuffer = new StringBuilder(m_idFactory.getDatabaseReferenceType());
			if ("INTEGER".equals(strBuffer.toString())) {
				strBuffer.append(" AUTO_INCREMENT");
			}
			else {
				if (m_idFactory.hasLength()) {
					strBuffer.append('(').append(m_idFactory.getLength()).append(')');
				}
			}

			return strBuffer.toString();
		}

		/**
		 * This method returns the database type associated with this converter.
		 */
		@Override
		public String getDatabaseType() {
			return m_idFactory.getDatabaseReferenceType();
		}
	}
}

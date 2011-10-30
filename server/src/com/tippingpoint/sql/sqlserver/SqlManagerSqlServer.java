package com.tippingpoint.sql.sqlserver;

import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.ColumnType;
import com.tippingpoint.database.ColumnTypeFactory;
import com.tippingpoint.database.ColumnTypeId;
import com.tippingpoint.database.IdFactory;
import com.tippingpoint.sql.base.SqlManager;

public class SqlManagerSqlServer extends SqlManager {
	/**
	 * This method constructs a new SQL Server builder.
	 */
	public SqlManagerSqlServer(final IdFactory idFactory) {
		super(idFactory);

		register(new IdColumnTypeConverter(idFactory));

		register(KEYWORD_MODIFY_COLUMN, "ALTER COLUMN");
		register(KEYWORD_MODIFY_CONSTRAINT, "ALTER");

		setSqlSchema(new SqlSchemaSqlServer(this));
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
		return "SELECT COLUMN_NAME, COLUMN_DEFAULT, IS_NULLABLE, COLUMNPROPERTY(OBJECT_ID(TABLE_SCHEMA + '.' + " +
				"TABLE_NAME), COLUMN_NAME, 'IsIdentity') ID_COLUMN, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH FROM " +
				"INFORMATION_SCHEMA.COLUMNS WHERE TABLE_CATALOG = '" + strDatabaseName + "' AND TABLE_NAME = '" +
				strTableName + "' ORDER BY COLUMNPROPERTY(OBJECT_ID(TABLE_SCHEMA + '.' + TABLE_NAME), " +
				"COLUMN_NAME, 'ColumnID')";
	}

	/**
	 * This method translates the JDBC type to a column type.
	 */
	@Override
	public ColumnType getType(final int nJdbcType, final String strTypeName) {
		ColumnType type = null;

		if (strTypeName.contains("identity")) {
			type = ColumnTypeFactory.getFactory().get(ColumnTypeId.TYPE);
		}
		else {
			type = super.getType(nJdbcType, strTypeName);
		}

		return type;
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
				strBuffer.append(" IDENTITY");
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

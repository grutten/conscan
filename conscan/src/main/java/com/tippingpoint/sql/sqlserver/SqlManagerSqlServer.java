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

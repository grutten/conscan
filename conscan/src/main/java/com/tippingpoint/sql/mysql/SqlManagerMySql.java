package com.tippingpoint.sql.mysql;

import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.ColumnTypeBoolean;
import com.tippingpoint.database.ColumnTypeId;
import com.tippingpoint.database.IdFactory;
import com.tippingpoint.sql.SqlAlter;
import com.tippingpoint.sql.base.SqlManager;

public class SqlManagerMySql extends SqlManager {
	/**
	 * This method constructs a new MySQL manager.
	 */
	public SqlManagerMySql(final IdFactory idFactory) {
		super(idFactory);

		register(new IdColumnTypeConverter(idFactory));
		register(new BooleanColumnTypeConverter());

		register(new SqlAlterExecutionFactory(), SqlAlter.class);

		setSqlSchema(new SqlSchemaMySql(this));
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

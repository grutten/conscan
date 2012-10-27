package com.tippingpoint.sql.oracle;

import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.ColumnTypeDate;
import com.tippingpoint.database.ColumnTypeId;
import com.tippingpoint.database.ColumnTypeInteger;
import com.tippingpoint.database.ColumnTypeString;
import com.tippingpoint.database.ColumnTypeText;
import com.tippingpoint.database.IdFactory;
import com.tippingpoint.sql.base.SqlManager;

public class SqlManagerOracle extends SqlManager {
	/**
	 * This method constructs a new Oracle manager.
	 */
	public SqlManagerOracle(IdFactory idFactory) {
		super(idFactory);

		register(new IdColumnTypeConverter(idFactory));
		register(new StaticColumnTypeConverter(ColumnTypeText.TYPE, "CLOB"));
		register(new StaticColumnTypeConverter(ColumnTypeDate.TYPE, "TIMESTAMP(6)"));
		register(new BooleanColumnTypeConverter("CHAR(1)"));
		register(new StaticColumnTypeConverter(ColumnTypeString.TYPE, "VARCHAR2"));
		register(new StaticColumnTypeConverter(ColumnTypeInteger.TYPE, "NUMBER"));

		setSqlSchema(new SqlSchemaOracle(this));
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

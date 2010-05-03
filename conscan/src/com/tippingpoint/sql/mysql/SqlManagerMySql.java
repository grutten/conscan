package com.tippingpoint.sql.mysql;

import com.tippingpoint.database.ColumnTypeId;
import com.tippingpoint.sql.SqlManager;

public class SqlManagerMySql extends SqlManager {
	/**
	 * This method constructs a new SQL Server builder.
	 */
	public SqlManagerMySql() {
		register(new StaticColumnTypeConverter(ColumnTypeId.class, "INTEGER AUTO_INCREMENT"));
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
	 * The string should contain {0}, which is replaced with the database name and {1}, which is replaced with the table
	 * name.
	 */
	@Override
	public String getTableDefinitionSql() {
		return "SELECT COLUMN_NAME, COLUMN_DEFAULT, IS_NULLABLE, IF(EXTRA = ''auto_increment'', 1, 0) ID_COLUMN, DATA_TYPE, "
				+ "CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ''{0}'' AND TABLE_NAME = ''{1}'' "
				+ "ORDER BY ORDINAL_POSITION";
	}
}

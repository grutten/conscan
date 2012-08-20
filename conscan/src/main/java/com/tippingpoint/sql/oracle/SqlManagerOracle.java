package com.tippingpoint.sql.oracle;

import com.tippingpoint.database.IdFactory;
import com.tippingpoint.sql.base.SqlManager;

public class SqlManagerOracle extends SqlManager {
	/**
	 * This method constructs a new Oracle manager.
	 */
	public SqlManagerOracle(IdFactory idFactory) {
		super(idFactory);
		
		setSqlSchema(new SqlSchemaOracle(this));
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
//	@Override
	public String getTableDefinitionSql(String strDatabaseName,
			String strTableName) {
		// TODO Auto-generated method stub
		return null;
	}
}

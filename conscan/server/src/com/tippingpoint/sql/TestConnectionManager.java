package com.tippingpoint.sql;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbutils.DbUtils;
import com.tippingpoint.database.DatabaseElementException;
import com.tippingpoint.sql.base.SqlManager;
import com.tippingpoint.sql.base.SqlSchema;
import com.tippingpoint.test.TestDbCase;

/**
 * This class is used to test out the Connection manager.
 */
public class TestConnectionManager extends TestDbCase {
	/**
	 * This method is used to test the reading of the schema.
	 */
	public void testSchema() {
		ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
		assertNotNull(manager);
		
		SqlManager sqlManager = manager.getSqlManager();
		assertNotNull(sqlManager);
		
		SqlSchema sqlSchema = sqlManager.getSqlSchema();
		assertNotNull(sqlSchema);
		
		Connection conn = null;
		try {
			conn = manager.getConnection();

			sqlSchema.getSchema(conn, UNIT_TEST_SCHEMA_NAME);
		}
		catch (SqlExecutionException e) {
			e.printStackTrace();
			fail();
		}
		catch (DatabaseElementException e) {
			e.printStackTrace();
			fail();
		}
		catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
		finally {
			DbUtils.closeQuietly(conn);
		}
	}
}

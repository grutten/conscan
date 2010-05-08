package com.tippingpoint.sql.base;

import java.util.Iterator;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.SqlBuilderException;
import com.tippingpoint.sql.SqlCreate;

/**
 * This class is used to execute the SQL create.
 */
public class SqlCreateExecution extends SqlExecution {
	/** This member holds the source of the command. */
	private SqlCreate m_sqlCreate;
	
	/**
	 * This method constructs a new execution for the given manager.
	 */
	public SqlCreateExecution(SqlManager sqlManager, SqlCreate sqlCreate) {
		super(sqlManager);

		m_sqlCreate = sqlCreate;
	}

	/**
	 * This method is used to generated the SQL statement.
	 * @throws SqlBuilderException 
	 */
	public String getSql() throws SqlBuilderException {
		Table table = m_sqlCreate.getTable();

		StringBuilder strSql = new StringBuilder();

		// the statement starts with the create statement
		strSql.append("CREATE TABLE ");
		strSql.append(table.getName());
		strSql.append('(');

		// add in the column statements next
		Iterator<Column> iterTableColumns = table.getColumns();
		while (iterTableColumns.hasNext()) {
			ColumnDefinition column = (ColumnDefinition)iterTableColumns.next();
			strSql.append(getPhrase(column));

			if (iterTableColumns.hasNext()) {
				strSql.append(", ");
			}
		}

		// add the various constraints to the table
		Constraint constraint = table.getPrimaryKey();
		if (constraint != null) {
			strSql.append(", ");
			strSql.append(getPhrase(constraint));
		}

		constraint = table.getLogicalKey();
		if (constraint != null) {
			strSql.append(", ");
			strSql.append(getPhrase(constraint));
		}

		Iterator<Constraint> iterConstraints = table.getConstraints();
		if (iterConstraints != null) {
			while (iterConstraints.hasNext()) {
				strSql.append(", ");
				strSql.append(getPhrase((Constraint)iterConstraints.next()));
			}
		}

		strSql.append(')');

		return strSql.toString();
	}
}

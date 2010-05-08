package com.tippingpoint.sql.base;

import java.util.Iterator;
import org.apache.commons.lang.StringUtils;
import com.tippingpoint.database.Column;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.ForeignKey;
import com.tippingpoint.database.ForeignKeyConstraint;
import com.tippingpoint.database.Index;
import com.tippingpoint.database.LogicalKeyConstraint;
import com.tippingpoint.database.PrimaryKeyConstraint;
import com.tippingpoint.sql.SqlBuilderException;

/**
 * This class is a base class used to generate and execute SQL statements.
 */
public abstract class SqlExecution {
	/** This member holds the manager used to create the execution. */
	protected SqlManager m_sqlManager;

	/**
	 * This method constructs a new execution for the given manager.
	 */
	public SqlExecution(SqlManager sqlManager) {
		m_sqlManager = sqlManager;
	}

	/**
	 * This method is used to generated the SQL statement.
	 * @throws SqlBuilderException 
	 */
	public abstract String getSql() throws SqlBuilderException;

	/**
	 * This method returns the text for a single column for inclusion in a
	 * SQL statement.
	 * @param column Column instance for which to create the phrase.
	 * @throws SqlBuilderException
	 */
	protected String getPhrase(ColumnDefinition column) throws SqlBuilderException {
			StringBuilder strSql = new StringBuilder();
			strSql.append(column.getName());
			strSql.append(' ');
	
			String strType = m_sqlManager.getType(column);
			if (StringUtils.isBlank(strType)) {
				throw new SqlBuilderException("Column type of '" + column.getType().getType() +
						"' not recognized for this database.");
			}
	
			strSql.append(strType);
	
			if (column.isRequired()) {
				strSql.append(" NOT");
			}
	
			strSql.append(" NULL");
	
			if (column.getDefault() != null) {
				strSql.append(' ').append(m_sqlManager.getKeyword(SqlManager.KEYWORD_COLUMN_DEFAULT)).append(' ');
				strSql.append(m_sqlManager.getConverter().convertToSqlString(column.getType(), column.getDefault()));
			}
	//		else if (!column.isRequired()) // only default to null if it is nullable
	//			strSql.append(" DEFAULT NULL");
	
			return strSql.toString();
		}

	/**
	 * This method returns the text for a constraint for inclusion in a
	 * SQL statement.
	 * @param constraint Constraint instance for which to create the phrase.
	 */
	protected String getPhrase(Constraint constraint) {
		StringBuilder strSql = new StringBuilder();
	
		String strType = null;
		if (constraint instanceof PrimaryKeyConstraint) {
			strType = "PRIMARY KEY";
		}
		else if (constraint instanceof LogicalKeyConstraint) {
			strType = "UNIQUE";
		}
		else if (constraint instanceof ForeignKeyConstraint) {
			strType = "FOREIGN KEY";
		}
		else if (constraint instanceof Index) {
			if (((Index)constraint).isUnique()) {
				strType = "UNIQUE";
			}
		}
	
		if (strType != null) {
			strSql.append("CONSTRAINT ");
	
			strSql.append(constraint.getName());
			strSql.append(' ');
			strSql.append(strType);
			strSql.append(" (");
	
			Iterator<Column> iterConstraintColumns = constraint.getColumns();
			while (iterConstraintColumns.hasNext()) {
				Column column = iterConstraintColumns.next();
				strSql.append(column.getName());
	
				if (iterConstraintColumns.hasNext()) {
					strSql.append(", ");
				}
			}
	
			strSql.append(")");
	
			// if this is a foreign key, the parent table must be referenced
			if (constraint instanceof ForeignKeyConstraint) {
				ForeignKeyConstraint keyConstraint = (ForeignKeyConstraint)constraint;
	
				strSql.append(" REFERENCES ");
				strSql.append(keyConstraint.getForeignTable().getName());
				strSql.append(" (");
	
				iterConstraintColumns = constraint.getColumns();
				while (iterConstraintColumns.hasNext()) {
					ForeignKey key = (ForeignKey)iterConstraintColumns.next();
					strSql.append(key.getParentColumn().getName());
	
					if (iterConstraintColumns.hasNext()) {
						strSql.append(", ");
					}
				}
	
				strSql.append(")");
			}
		}
	
		return strSql.toString();
	}
}

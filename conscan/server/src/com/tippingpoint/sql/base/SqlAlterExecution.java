package com.tippingpoint.sql.base;

import java.util.List;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.SqlAlter;
import com.tippingpoint.sql.SqlBuilderException;

/**
 * This class is used to execute the SQL used to alter table construction.
 */
public class SqlAlterExecution extends SqlExecution {
	/** This member holds the source of the command. */
	private final SqlAlter m_sqlAlter;

	/**
	 * This method constructs a new execution for the given manager.
	 */
	public SqlAlterExecution(final SqlManager sqlManager, final SqlAlter sqlAlter) {
		super(sqlManager);

		m_sqlAlter = sqlAlter;
	}

	/**
	 * This method is used to generated the SQL statement.
	 * 
	 * @throws SqlBuilderException
	 */
	@Override
	protected String generateSql() throws SqlBuilderException {
		final Table table = m_sqlAlter.getTable();

		final StringBuilder strSql = new StringBuilder();

		// the statement starts with the create statement
		strSql.append("ALTER TABLE ");
		strSql.append(table.getName());

		int nCount = append("ADD", m_sqlAlter.getNewColumns(), strSql, false);
		nCount +=
			append(m_sqlManager.getKeyword(SqlManager.KEYWORD_MODIFY_COLUMN), m_sqlAlter.getModifyColumns(), strSql,
					nCount > 0);
		nCount += appendConstraint("ADD", m_sqlAlter.getNewConstraints(), strSql, nCount > 0);
		nCount +=
			appendConstraint(m_sqlManager.getKeyword(SqlManager.KEYWORD_MODIFY_CONSTRAINT), m_sqlAlter
					.getModifyConstraints(), strSql, nCount > 0);
		nCount += appendDroppedConstraint(strSql, nCount > 0);

		return strSql.toString();
	}

	/**
	 * This method returns the phrase used to drop the constraint. The DROP keyword is not included.
	 * 
	 * @param constraint Constraint to be dropped.
	 */
	protected String getDropPhrase(final Constraint constraint) {
		// default action is to simply return the name of the constraint
		return constraint.getName();
	}

	/**
	 * This method adds the list of columns to the SQL statement.
	 * 
	 * @throws SqlBuilderException
	 */
	private int append(final String strIntroduction, final List<ColumnDefinition> listColumns,
			final StringBuilder strSql, final boolean bAddSeparator) throws SqlBuilderException {
		if (listColumns.size() > 0) {
			if (bAddSeparator) {
				strSql.append(",");
			}

			strSql.append(' ').append(strIntroduction).append(' ');

			for (int nIndex = 0; nIndex < listColumns.size(); ++nIndex) {
				final ColumnDefinition column = listColumns.get(nIndex);

				if (nIndex > 0) {
					strSql.append(", ");
				}

				strSql.append(getPhrase(column));
			}
		}

		return listColumns.size();
	}

	/**
	 * This method adds the list of constraints to the SQL statement.
	 * 
	 * @throws SqlBuilderException
	 */
	private int appendConstraint(final String strIntroduction, final List<Constraint> listConstraints,
			final StringBuilder strSql, final boolean bAddSeparator) throws SqlBuilderException {
		if (listConstraints.size() > 0) {
			if (bAddSeparator) {
				strSql.append(",");
			}

			strSql.append(' ').append(strIntroduction).append(' ');

			for (int nIndex = 0; nIndex < listConstraints.size(); ++nIndex) {
				final Constraint constraint = listConstraints.get(nIndex);

				if (nIndex > 0) {
					strSql.append(", ");
				}

				strSql.append(getPhrase(constraint));
			}
		}

		return listConstraints.size();
	}

	/**
	 * This method adds the list of constraints to the SQL statement for dropped constraints
	 * 
	 * @throws SqlBuilderException
	 */
	private int appendDroppedConstraint(final StringBuilder strSql, final boolean bAddSeparator)
			throws SqlBuilderException {
		final List<Constraint> listDroppedConstraints = m_sqlAlter.getDroppedConstraints();
		if (!listDroppedConstraints.isEmpty()) {
			if (bAddSeparator) {
				strSql.append(",");
			}

			strSql.append(' ').append("DROP").append(' ');

			for (int nIndex = 0; nIndex < listDroppedConstraints.size(); ++nIndex) {
				final Constraint constraint = listDroppedConstraints.get(nIndex);

				if (nIndex > 0) {
					strSql.append(", ");
				}

				strSql.append(getDropPhrase(constraint));
			}
		}

		return listDroppedConstraints.size();
	}
}

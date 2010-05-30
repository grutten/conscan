package com.tippingpoint.sql.mysql;

import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.ForeignKeyConstraint;
import com.tippingpoint.database.PrimaryKeyConstraint;
import com.tippingpoint.sql.SqlAlter;
import com.tippingpoint.sql.base.SqlManager;

public class SqlAlterExecution extends com.tippingpoint.sql.base.SqlAlterExecution {
	/**
	 * This method
	 * 
	 * @param sqlManager
	 * @param sqlAlter
	 */
	public SqlAlterExecution(final SqlManager sqlManager, final SqlAlter sqlAlter) {
		super(sqlManager, sqlAlter);
	}

	/**
	 * This method returns the phrase used to drop the constraint. The DROP keyword is not included.
	 * 
	 * @param constraint Constraint to be dropped.
	 */
	@Override
	protected String getDropPhrase(final Constraint constraint) {
		String strPhrase = null;

		if (constraint instanceof PrimaryKeyConstraint) {
			strPhrase = "PRIMARY KEY";
		}
		else if (constraint instanceof ForeignKeyConstraint) {
			strPhrase = "FOREIGN KEY " + constraint.getName();
		}
		else {
			strPhrase = "KEY " + constraint.getName();
		}

		return strPhrase;
	}
}

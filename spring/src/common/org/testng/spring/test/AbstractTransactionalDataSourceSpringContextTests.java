/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.testng.spring.test;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Subclass of AbstractTransactionalSpringContextTests that adds some convenience
 * functionality. Expects a DataSource to be defined in the Spring application context.
 *
 * <p>This class exposes a JdbcTemplate and provides an easy way to
 * delete from the database in a new transaction.
 *
 * @author Rod Johnson
 * @since 1.1.1
 */
public abstract class AbstractTransactionalDataSourceSpringContextTests
    extends AbstractTransactionalSpringContextTests {

	protected JdbcTemplate jdbcTemplate;


	/**
	 * Did this test delete any tables? If so, we forbid transaction completion,
	 * and only allow rollback.
	 */
	private boolean zappedTables;


	/**
	 * Setter: DataSource is provided by Dependency Injection.
	 * @param dataSource
	 */
	public void setDataSource(DataSource dataSource) {
		// TODO what if you want to use a JdbcTemplate by preference,
		// for a native extractor?
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}


	/**
	 * Convenient method to delete all rows from these tables.
	 * Calling this method will make avoidance of rollback by calling
	 * <code>setComplete()</code> impossible.
	 * @see #setComplete
	 */
	protected void deleteFromTables(String[] names) {
		for (int i = 0; i < names.length; i++) {
			if (logger.isInfoEnabled()) {
				int rowCount = this.jdbcTemplate.update("DELETE FROM " + names[i]);
				logger.info("Deleted " + rowCount + " rows from table " + names[i]);
			}
		}
		this.zappedTables = true;
	}

	/**
	 * Overridden to prevent the transaction committing if a number of tables have been
	 * cleared, as a defensive measure against accidental <i>permanent</i> wiping of a database.
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#setComplete()
	 */
	protected final void setComplete() {
		if (this.zappedTables) {
			throw new IllegalStateException("Cannot set complete after deleting tables");
		}
		super.setComplete();
	}

}

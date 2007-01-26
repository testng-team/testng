package org.testng.spring.test;

import java.util.Map;
import java.lang.reflect.Method;
import javax.sql.DataSource;

import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.test.annotation.*;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * @author Hani Suleiman
 *         Date: Jan 26, 2007
 *         Time: 8:17:20 AM
 */
public abstract class AbstractAnnotationAwareTransactionalTests
		extends AbstractTransactionalDataSourceSpringContextTests {
	
	protected SimpleJdbcTemplate simpleJdbcTemplate;
	
	private TransactionAttributeSource transactionAttributeSource = new AnnotationTransactionAttributeSource();
	
	protected ProfileValueSource profileValueSource = SystemProfileValueSource.getInstance();


	@Override
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(dataSource);
		// JdbcTemplate will be identically configured
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(this.jdbcTemplate);
	}

	// TODO code to try to load (and cache!) ProfileValueSource
	// from a given URL? It's easy enough to do, of course.
	protected void findUniqueProfileValueSourceFromContext(ApplicationContext ac) {
		Map beans = ac.getBeansOfType(ProfileValueSource.class);
		if (beans.size() == 1) {
			this.profileValueSource = (ProfileValueSource) beans.values().iterator().next();
		}
	}

	@BeforeMethod
  protected void checkTransaction(Method testMethod) throws Throwable {		
		TransactionDefinition explicitTransactionDefinition =
				this.transactionAttributeSource.getTransactionAttribute(testMethod, getClass());
		if (explicitTransactionDefinition != null) {
			logger.info("Custom transaction definition [" + explicitTransactionDefinition + " for test method " + testMethod.getName());
			setTransactionDefinition(explicitTransactionDefinition);
		}
		else if (testMethod.isAnnotationPresent(NotTransactional.class)) {
			// Don't have any transaction...
			preventTransaction();
		}		
	}

  @AfterMethod
  protected void checkDirtiesContext(Method testMethod) {
    if (testMethod.isAnnotationPresent(DirtiesContext.class)) {
      AbstractAnnotationAwareTransactionalTests.this.setDirty();
    }    
  }  
}

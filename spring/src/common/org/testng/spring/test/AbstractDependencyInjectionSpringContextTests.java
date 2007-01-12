/*
 * Copyright 2002-2007 the original author or authors.
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * Convenient superclass for tests depending on a Spring context.
 * The test instance itself is populated by Dependency Injection.
 *
 * <p>Really for integration testing, not unit testing.
 * You should <i>not</i> normally use the Spring container
 * for unit tests: simply populate your POJOs in plain JUnit tests!
 *
 * <p>This supports two modes of populating the test:
 * <ul>
 * <li>Via Setter Dependency Injection. Simply express dependencies on objects
 * in the test fixture, and they will be satisfied by autowiring by type.
 * <li>Via Field Injection. Declare protected variables of the required type
 * which match named beans in the context. This is autowire by name,
 * rather than type. This approach is based on an approach originated by
 * Ara Abrahmian. Setter Dependency Injection is the default: set the
 * "populateProtectedVariables" property to true in the constructor to switch
 * on Field Injection.
 * </ul>
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Rick Evans
 * @since 1.1.1
 * @see #setDirty
 * @see #contextKey
 * @see #getContext
 * @see #getConfigLocations
 */
public abstract class AbstractDependencyInjectionSpringContextTests extends AbstractSingleSpringContextTests {

	/**
	 * Constant that indicates no autowiring at all.
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_NO = 0;

	/**
	 * Constant that indicates autowiring bean properties by name.
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

	/**
	 * Constant that indicates autowiring bean properties by type.
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;


	private boolean populateProtectedVariables = false;

	private int autowireMode = AUTOWIRE_BY_TYPE;

	private boolean dependencyCheck = true;

	private String[] managedVariableNames;


	/**
	 * Default constructor for AbstractDependencyInjectionSpringContextTests.
	 */
	public AbstractDependencyInjectionSpringContextTests() {
	}

	/**
	 * Set whether to populate protected variables of this test case.
	 * Default is "false".
	 */
	public final void setPopulateProtectedVariables(boolean populateFields) {
		this.populateProtectedVariables = populateFields;
	}

	/**
	 * Return whether to populate protected variables of this test case.
	 */
	public final boolean isPopulateProtectedVariables() {
		return populateProtectedVariables;
	}

	/**
	 * Set the autowire mode for test properties set by Dependency Injection.
	 * <p>The default is "AUTOWIRE_BY_TYPE". Can be set to "AUTOWIRE_BY_NAME"
	 * or "AUTOWIRE_NO" instead.
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_NO
	 */
	public final void setAutowireMode(int autowireMode) {
		this.autowireMode = autowireMode;
	}

	/**
	 * Return the autowire mode for test properties set by Dependency Injection.
	 */
	public final int getAutowireMode() {
		return autowireMode;
	}

	/**
	 * Set whether or not dependency checking should be performed
	 * for test properties set by Dependency Injection.
	 * <p>The default is "true", meaning that tests cannot be run
	 * unless all properties are populated.
	 */
	public final void setDependencyCheck(boolean dependencyCheck) {
		this.dependencyCheck = dependencyCheck;
	}

	/**
	 * Return whether or not dependency checking should be performed
	 * for test properties set by Dependency Injection.
	 */
	public final boolean isDependencyCheck() {
		return dependencyCheck;
	}


	/**
	 * Prepare this test instance, injecting dependencies into its
	 * protected fields and its bean properties.
	 */
	protected void prepareTestInstance() throws Exception {
		injectDependencies();
	}

	/**
	 * Inject dependencies into 'this' instance (that is, this test instance).
	 * <p>The default implementation populates protected variables if the
	 * {@link #populateProtectedVariables() appropriate flag is set}, else
	 * uses autowiring if autowiring is switched on (which it is by default).
	 * <p>You can certainly override this method if you want to totally control
	 * how dependencies are injected into 'this' instance.
	 * @throws Exception in the case of any errors
	 * @see #populateProtectedVariables() 
	 */
	private void injectDependencies() throws Exception {
		if (isPopulateProtectedVariables()) {
			if (this.managedVariableNames == null) {
				initManagedVariableNames();
			}
			populateProtectedVariables();
		}
		else if (getAutowireMode() != AUTOWIRE_NO) {
			getApplicationContext().getBeanFactory().autowireBeanProperties(
					this, getAutowireMode(), isDependencyCheck());
		}
	}

	private void initManagedVariableNames() throws IllegalAccessException {
		LinkedList managedVarNames = new LinkedList();
		Class clazz = getClass();

		do {
			Field[] fields = clazz.getDeclaredFields();
			if (logger.isDebugEnabled()) {
				logger.debug("Found " + fields.length + " fields on " + clazz);
			}

			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				field.setAccessible(true);
				if (logger.isDebugEnabled()) {
					logger.debug("Candidate field: " + field);
				}
				if (isProtectedInstanceField(field)) {
					Object oldValue = field.get(this);
					if (oldValue == null) {
						managedVarNames.add(field.getName());
						if (logger.isDebugEnabled()) {
							logger.debug("Added managed variable '" + field.getName() + "'");
						}
					}
					else {
						if (logger.isDebugEnabled()) {
							logger.debug("Rejected managed variable '" + field.getName() + "'");
						}
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		while (!clazz.equals(AbstractDependencyInjectionSpringContextTests.class));

		this.managedVariableNames = (String[]) managedVarNames.toArray(new String[managedVarNames.size()]);
	}

	private boolean isProtectedInstanceField(Field field) {
		int modifiers = field.getModifiers();
		return !Modifier.isStatic(modifiers) && Modifier.isProtected(modifiers);
	}

	private void populateProtectedVariables() throws IllegalAccessException {
		for (int i = 0; i < this.managedVariableNames.length; i++) {
			String varName = this.managedVariableNames[i];
			Object bean = null;
			try {
				Field field = findField(getClass(), varName);
				bean = getApplicationContext().getBean(varName, field.getType());
				field.setAccessible(true);
				field.set(this, bean);
				if (logger.isDebugEnabled()) {
					logger.debug("Populated field: " + field);
				}
			}
			catch (NoSuchFieldException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("No field with name '" + varName + "'");
				}
			}
			catch (NoSuchBeanDefinitionException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("No bean with name '" + varName + "'");
				}
			}
		}
	}

	private Field findField(Class clazz, String name) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(name);
		}
		catch (NoSuchFieldException ex) {
			Class superclass = clazz.getSuperclass();
			if (superclass != AbstractSpringContextTests.class) {
				return findField(superclass, name);
			}
			else {
				throw ex;
			}
		}
	}

}

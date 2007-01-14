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

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

/**
 * Abstract JUnit test class that holds and exposes a single Spring
 * {@link org.springframework.context.ApplicationContext ApplicationContext}.
 *
 * <p>This class will cache contexts based on a <i>context key</i>: normally the
 * config locations String array describing the Spring resource descriptors making
 * up the context. Unless the {@link #setDirty()} method is called by a test, the
 * context will not be reloaded, even across different subclasses of this test.
 * This is particularly beneficial if your context is slow to construct, for example
 * if you are using Hibernate and the time taken to load the mappings is an issue.
 *
 * <p>For such standard usage, simply override the {@link #getConfigLocations()}
 * method and provide the desired config files.
 *
 * <p>If you don't want to load a standard context from an array of config locations,
 * you can override the {@link #contextKey()} method. In conjunction with this you
 * typically need to override the {@link #loadContext(Object)} method, which by
 * default loads the locations specified in the {@link #getConfigLocations()} method.
 *
 * <p><b>WARNING:</b> When doing integration tests from within Eclipse, only use
 * classpath resource URLs. Else, you may see misleading failures when changing
 * context locations.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 2.0
 * @see #getConfigLocations()
 * @see #contextKey()
 * @see #loadContext(Object)
 * @see #getApplicationContext()
 */
public abstract class AbstractSingleSpringContextTests extends AbstractSpringContextTests {

	/** Application context this test will run against */
	protected ConfigurableApplicationContext applicationContext;

	private int loadCount = 0;


	/**
	 * Default constructor for AbstractDependencyInjectionSpringContextTests.
	 */
	public AbstractSingleSpringContextTests() {
	}

	/**
	 * This implementation is final.
	 * Override <code>onSetUp</code> for custom behavior.
	 * @see #onSetUp()
	 */
  @BeforeMethod(groups="spring-init")
  protected final void setUp() throws Exception {
		this.applicationContext = getContext(contextKey());
		prepareTestInstance();
		onSetUp();
	}

	/**
	 * Prepare this test instance, for example populating its fields.
	 * The context has already been loaded at the time of this callback.
	 * <p>This implementation does nothing.
	 */
	protected void prepareTestInstance() throws Exception {
	}

	/**
	 * Subclasses can override this method in place of the
	 * <code>setUp()</code> method, which is final in this class.
	 * This implementation does nothing.
	 * @throws Exception simply let any exception propagate
	 */
	protected void onSetUp() throws Exception {
	}

	/**
	 * Called to say that the "applicationContext" instance variable is dirty and
	 * should be reloaded. We need to do this if a test has modified the context
	 * (for example, by replacing a bean definition).
	 */
	protected void setDirty() {
		setDirty(contextKey());
	}

	/**
	 * This implementation is final.
	 * Override <code>onTearDown</code> for custom behavior.
	 * @see #onTearDown()
	 */
  @AfterMethod(groups="spring-init")
  protected final void tearDown() throws Exception {
		onTearDown();
	}

	/**
	 * Subclasses can override this to add custom behavior on teardown.
	 * @throws Exception simply let any exception propagate
	 */
	protected void onTearDown() throws Exception {
	}


	/**
	 * Return a key for this context. Default is the config location array
	 * as determined by {@link #getConfigLocations()}.
	 * <p>If you override this method, you will typically have to override
	 * {@link #loadContext(Object)} as well, being able to handle the key type
	 * that this method returns.
	 * @see #getConfigLocations()
	 */
	protected Object contextKey() {
		return getConfigLocations();
	}

	/**
	 * This implementation assumes a key of type String array and loads
	 * a context from the given locations.
	 * <p>If you override {@link #contextKey()}, you will typically have to
	 * override this method as well, being able to handle the key type
	 * that <code>contextKey()</code> returns.
	 * @see #getConfigLocations()
	 */
	protected ConfigurableApplicationContext loadContext(Object key) throws Exception {
		return loadContextLocations((String[]) key);
	}

	/**
	 * Subclasses must implement this method to return the locations of their
	 * config files, unless they override {@link #contextKey()} and
	 * {@link #loadContext(Object)} instead.
	 * <p>A plain path will be treated as class path location, e.g.:
	 * "org/springframework/whatever/foo.xml". Note however that you may prefix path
	 * locations with standard Spring resource prefixes. Therefore, a config location
	 * path prefixed with "classpath:" with behave the same as a plain path, but a
	 * config location such as "file:/some/path/path/location/appContext.xml" will
	 * be treated as a filesystem location.
	 * <p>The default implementation returns an empty array.
	 * @return an array of config locations
	 */
	protected String[] getConfigLocations() {
		return new String[0];
	}

	/**
	 * Load an ApplicationContext from the given config locations.
	 * @param locations the config locations
	 * @return the corresponding ApplicationContext instance (potentially cached)
	 */
	protected ConfigurableApplicationContext loadContextLocations(String[] locations) throws Exception {
		++this.loadCount;
		if (logger.isInfoEnabled()) {
			logger.info("Loading context for: " + StringUtils.arrayToCommaDelimitedString(locations));
		}
		return new ClassPathXmlApplicationContext(locations);
	}

	/**
	 * Return the ApplicationContext that this base class manages.
	 */
	public final ConfigurableApplicationContext getApplicationContext() {
		return this.applicationContext;
	}

	/**
	 * Return the current number of context load attempts.
	 */
	public final int getLoadCount() {
		return this.loadCount;
	}

}

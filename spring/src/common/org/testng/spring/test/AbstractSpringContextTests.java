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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

/**
 * Superclass for TestNG test cases using a Spring context.
 *
 * <p>Maintains a static cache of contexts by key. This has significant performance
 * benefit if initializing the context would take time. While initializing a 
 * Spring context itself is very quick, some beans in a context, such as
 * a LocalSessionFactoryBean for working with Hibernate, may take time to
 * initialize. Hence it often makes sense to do that initializing once.
 *
 * <p>Normally you won't extend this class directly, but extend one
 * of its subclasses.
 *
 * @author Rod Johnson
 * @since 1.1.1
 * @see AbstractDependencyInjectionSpringContextTests
 * @see AbstractTransactionalSpringContextTests
 * @see AbstractTransactionalDataSourceSpringContextTests
 */
public abstract class AbstractSpringContextTests {

  /**
   * Map of context keys returned by subclasses of this class, to
   * Spring Contexts. This needs to be static, as JUnit tests are
   * destroyed and recreated between running individual test methods.
   */
  private static Map contextKeyToContextMap = new HashMap();

  /**
   * Logger available to subclasses.
   */
  protected final Log logger = LogFactory.getLog(getClass());


  /**
   * Set custom locations dirty. This will cause them to be reloaded
   * from the cache before the next test case is executed.
   * <p>Call this method only if you change the state of a singleton
   * bean, potentially affecting future tests.
   */
  protected void setDirty(String[] locations) {
    String keyString = contextKeyString(locations);
    ConfigurableApplicationContext ctx =
        (ConfigurableApplicationContext) contextKeyToContextMap.remove(keyString);
    if (ctx != null) {
      ctx.close();
    }
  }

  protected boolean hasCachedContext(Object contextKey) {
    return contextKeyToContextMap.containsKey(contextKey);
  }

  /**
   * Subclasses can override this to return a String representation of
   * their contextKey for use in logging
   */
  protected String contextKeyString(Object contextKey) {
    if (contextKey instanceof String[]) {
      return StringUtils.arrayToCommaDelimitedString((String[]) contextKey);
    }
    else {
      return contextKey.toString();
    }
  }

  protected ConfigurableApplicationContext getContext(Object key) {
    String keyString = contextKeyString(key);
    ConfigurableApplicationContext ctx =
        (ConfigurableApplicationContext) contextKeyToContextMap.get(keyString);
    if (ctx == null) {
      if (key instanceof String[]) {
        ctx = loadContextLocations((String[]) key);
      }
      else {
        ctx = loadContext(key);
      }
      contextKeyToContextMap.put(keyString, ctx);
    }
    return ctx;
  }


  /**
   * Subclasses can invoke this to get a context key for the given location.
   * This doesn't affect the applicationContext instance variable in this class.
   * Dependency Injection cannot be applied from such contexts.
   */
  protected ConfigurableApplicationContext loadContextLocations(String[] locations) {
    if (logger.isInfoEnabled()) {
      logger.info("Loading config for: " + StringUtils.arrayToCommaDelimitedString(locations));
    }
    return new ClassPathXmlApplicationContext(locations);
  }

  protected ConfigurableApplicationContext loadContext(Object key) {
    throw new UnsupportedOperationException("Subclasses may override this");
  }

}

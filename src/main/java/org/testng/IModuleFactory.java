package org.testng;

import com.google.inject.Module;

/**
 * This interface is used by the moduleFactory attribute of the @Guice annotation. It allows users
 * to use different Guice modules based on the test class waiting to be injected.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public interface IModuleFactory {

  /**
   * @param testClass The teset class
   *
   * @return The class of the Guice module to instantiate in order to get an instance of this
   * test class.
   */
  Class<? extends Module> createModule(Class<?> testClass); 
}

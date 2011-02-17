package org.testng;

import com.google.inject.Module;

/**
 * This interface is used by the moduleFactory attribute of the @Guice
 * annotation. It allows users to use different Guice modules based on the test
 * class waiting to be injected.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public interface IModuleFactory {

  /**
   * @param context The current test context
   * @param testClass The test class
   *
   * @return The Guice module that should be used to get an instance of this
   * test class.
   */
  Module createModule(ITestContext context, Class<?> testClass);
}

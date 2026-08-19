package org.testng;

import com.google.inject.Module;
import org.jspecify.annotations.Nullable;

/**
 * This interface is used by the moduleFactory attribute of the @Guice annotation. It allows users
 * to use different Guice modules based on the test class waiting to be injected.
 */
public interface IModuleFactory {

  /**
   * @param context The current test context
   * @param testClass The test class
   * @return The Guice module that should be used to get an instance of this test class.
   */
  Module createModule(@Nullable ITestContext context, Class<?> testClass);
}

package org.testng;

import java.lang.reflect.Method;
import org.testng.internal.ClonedMethod;

/**
 * Holds helper methods for code that builds its own test methods, for example a method interceptor.
 */
public class TestNGUtils {

  /**
   * Creates a test method that runs {@code method} on the test instance of {@code existingMethod}.
   *
   * <p>{@code method} must belong to the class of {@code existingMethod}, because the new test
   * method runs on the same test instance. The new test method shares the test class, the groups
   * and the time-out of {@code existingMethod}. It runs one time, has no dependencies, and is not a
   * configuration method.
   *
   * @param existingMethod the test method that gives the test class and the test instance.
   * @param method the Java method that the new test method runs.
   * @return the new test method.
   */
  public static ITestNGMethod createITestNGMethod(ITestNGMethod existingMethod, Method method) {
    return new ClonedMethod(existingMethod, method);
  }
}

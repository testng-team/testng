package org.testng;

import java.lang.reflect.Method;
import org.testng.internal.ClonedMethod;

public class TestNGUtils {

  /**
   * Create an ITestNGMethod for @code{method} based on @code{existingMethod}, which needs to belong
   * to the same class.
   *
   * @param existingMethod The test method
   * @param method The method
   * @return The created test method
   */
  public static ITestNGMethod createITestNGMethod(ITestNGMethod existingMethod, Method method) {
    return new ClonedMethod(existingMethod, method);
  }
}

package org.testng;

import org.testng.internal.ClonedMethod;

import java.lang.reflect.Method;

public class TestNGUtils {

  /**
   * Create an ITestNGMethod for @code{method} based on @code{existingMethod}, which needs
   * to belong to the same class.
   */
  public static ITestNGMethod createITestNGMethod(ITestNGMethod existingMethod, Method method) {
    return new ClonedMethod(existingMethod, method);
  }
}

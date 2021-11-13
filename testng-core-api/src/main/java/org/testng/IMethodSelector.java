package org.testng;

import java.util.List;

/**
 * This interface is used to augment or replace TestNG's algorithm to decide whether a test method
 * should be included in a test run.
 */
public interface IMethodSelector {

  /**
   * @param context The selector context. The implementation of this method can invoke
   *     setHalted(true) to indicate that no other Method Selector should be invoked by TestNG after
   *     this one. Additionally, this implementation can manipulate the Map object returned by
   *     getUserData().
   * @param method The test method
   * @param isTestMethod true if this is a @Test method, false if it's a configuration method
   * @return true if this method should be included in the test run, false otherwise
   */
  boolean includeMethod(IMethodSelectorContext context, ITestNGMethod method, boolean isTestMethod);

  /**
   * Invoked when all the test methods are known so that the method selector can perform additional
   * work, such as adding the transitive closure of all the groups being included and depended upon.
   *
   * @param testMethods The test methods
   */
  void setTestMethods(List<ITestNGMethod> testMethods);
}

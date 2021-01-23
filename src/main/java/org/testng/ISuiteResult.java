package org.testng;

/**
 * This class represents the result of a suite run.
 */
public interface ISuiteResult {

  /**
   * @return The name of the property file for these tests.
   * @deprecated - This method is deprecated as of "7.4.0"
   */
  @Deprecated
  default String getPropertyFileName() {
    return "";
  }

  /**
   * @return The testing context for these tests.
   */
  ITestContext getTestContext();
}

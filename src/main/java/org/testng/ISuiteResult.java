package org.testng;

/** This class represents the result of a suite run. */
public interface ISuiteResult {

  /** @return The name of the property file for these tests. */
  String getPropertyFileName();

  /** @return The testing context for these tests. */
  ITestContext getTestContext();
}

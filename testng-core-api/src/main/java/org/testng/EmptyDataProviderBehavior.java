package org.testng;

/**
 * Defines the behavior when a {@link org.testng.annotations.DataProvider} returns no data (an empty
 * array or iterator).
 */
public enum EmptyDataProviderBehavior {
  /** The test method is ignored and no {@link ITestResult} is produced. */
  IGNORE,

  /**
   * Exactly one {@link ITestResult} with status {@link ITestResult#SKIP} is produced for the test
   * method, and {@link ITestListener#onTestSkipped(ITestResult)} is invoked.
   */
  SKIP
}

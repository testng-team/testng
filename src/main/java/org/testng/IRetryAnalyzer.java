package org.testng;

/**
 * Interface to implement to be able to have a chance to retry a failed test.
 *
 * @author tocman@gmail.com (Jeremie Lenfant-Engelmann)
 *
 */
public interface IRetryAnalyzer {

  /**
   * Returns true if the test method has to be retried, false otherwise.
   *
   * @param result The result of the test method that just ran.
   * @return true if the test method has to be retried, false otherwise.
   */
  public boolean retry(ITestResult result);
}

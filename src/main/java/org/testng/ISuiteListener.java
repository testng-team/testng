package org.testng;


/**
 * Listener for test suites.
 *
 * @author Cedric Beust, Aug 6, 2004
 *
 */
public interface ISuiteListener extends ITestNGListener {
  /**
   * This method is invoked before the SuiteRunner starts.
   */
  public void onStart(ISuite suite);

  /**
   * This method is invoked after the SuiteRunner has run all
   * the test suites.
   */
  public void onFinish(ISuite suite);

}

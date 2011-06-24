package org.testng;

/**
 * A listener used to monitor when a TestNG run starts and ends.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public interface IExecutionListener extends ITestNGListener {

  /**
   * Invoked before the TestNG run starts.
   */
  void onExecutionStart();

  /**
   * Invoked once all the suites have been run.
   */
  void onExecutionFinish();

}

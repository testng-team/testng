package org.testng;

/**
 * A listener used to monitor when a TestNG run starts and ends.
 * When implementation of this listener is wired into TestNG, TestNG will ensure that
 * <ul>
 * <li>{@link IExecutionListener#onExecutionStart()} gets invoked before TestNG proceeds with invoking any other
 * listener.</li>
 * <li>{@link IExecutionListener#onExecutionFinish()} gets invoked at the very last (after report generation phase),
 * before TestNG exits the JVM.
 * </li>
 * </ul>
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

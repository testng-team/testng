package org.testng;

/**
 * A listener used to monitor when a TestNG run starts and ends. When implementation of this
 * listener is wired into TestNG, TestNG will ensure that
 *
 * <ul>
 *   <li>{@link IExecutionListener#onExecutionStart()} gets invoked before TestNG proceeds with
 *       invoking any other listener.
 *   <li>{@link IExecutionListener#onExecutionFinish()} gets invoked at the very last (after report
 *       generation phase), before TestNG exits the JVM.
 * </ul>
 */
public interface IExecutionListener extends ITestNGListener {

  /** Invoked before the TestNG run starts. */
  default void onExecutionStart() {
    // not implemented
  }

  /** Invoked once all the suites have been run. */
  default void onExecutionFinish() {
    // not implemented
  }
}

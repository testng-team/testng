package org.testng;

/** Listener interface for events related to configuration methods. */
public interface IConfigurationListener extends ITestNGListener {

  /** Invoked whenever a configuration method succeeded. */
  default void onConfigurationSuccess(ITestResult itr) {
    // not implemented
  }

  /** Invoked whenever a configuration method failed. */
  default void onConfigurationFailure(ITestResult itr) {
    // not implemented
  }

  /** Invoked whenever a configuration method was skipped. */
  default void onConfigurationSkip(ITestResult itr) {
    // not implemented
  }

  /** Invoked before a configuration method is invoked. */
  default void beforeConfiguration(ITestResult tr) {
    // not implemented
  }
}

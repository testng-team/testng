package org.testng;

import org.jspecify.annotations.Nullable;

/** Listener interface for events related to configuration methods. */
public interface IConfigurationListener extends ITestNGListener {

  /**
   * Invoked whenever a configuration method succeeded.
   *
   * @param tr The test result
   */
  default void onConfigurationSuccess(ITestResult tr) {
    // not implemented
  }

  /**
   * Invoked whenever a configuration method succeeded.
   *
   * @param tr The test result
   * @param tm The test method, or {@code null} when the configuration method is not bound to one
   */
  default void onConfigurationSuccess(ITestResult tr, @Nullable ITestNGMethod tm) {
    // not implemented
  }

  /**
   * Invoked whenever a configuration method failed.
   *
   * @param tr The test result
   */
  default void onConfigurationFailure(ITestResult tr) {
    // not implemented
  }

  /**
   * Invoked whenever a configuration method failed.
   *
   * @param tr The test result
   * @param tm The test method, or {@code null} when the configuration method is not bound to one
   */
  default void onConfigurationFailure(ITestResult tr, @Nullable ITestNGMethod tm) {
    // not implemented
  }

  /**
   * Invoked whenever a configuration method was skipped.
   *
   * @param tr The test result
   */
  default void onConfigurationSkip(ITestResult tr) {
    // not implemented
  }

  /**
   * Invoked whenever a configuration method was skipped.
   *
   * @param tr The test result
   * @param tm The test method, or {@code null} when the configuration method is not bound to one
   */
  default void onConfigurationSkip(ITestResult tr, @Nullable ITestNGMethod tm) {
    // not implemented
  }

  /**
   * Invoked before a configuration method is invoked.
   *
   * @param tr The test result
   */
  default void beforeConfiguration(ITestResult tr) {
    // not implemented
  }

  /**
   * Invoked before a configuration method is invoked.
   *
   * @param tr The test result
   * @param tm The test method, or {@code null} when the configuration method is not bound to one
   */
  default void beforeConfiguration(ITestResult tr, @Nullable ITestNGMethod tm) {
    // not implemented
  }
}

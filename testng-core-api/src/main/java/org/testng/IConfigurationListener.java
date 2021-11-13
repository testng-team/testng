package org.testng;

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
   * @param tm The test method
   */
  default void onConfigurationSuccess(ITestResult tr, ITestNGMethod tm) {
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
   * @param tm The test method
   */
  default void onConfigurationFailure(ITestResult tr, ITestNGMethod tm) {
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
   * @param tm The test method
   */
  default void onConfigurationSkip(ITestResult tr, ITestNGMethod tm) {
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
   * @param tm The test method
   */
  default void beforeConfiguration(ITestResult tr, ITestNGMethod tm) {
    // not implemented
  }
}

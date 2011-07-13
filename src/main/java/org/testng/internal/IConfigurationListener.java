package org.testng.internal;

import org.testng.ITestNGListener;
import org.testng.ITestResult;

/**
 * Listener interface for events related to configuration methods.
 */
public interface IConfigurationListener extends ITestNGListener {
  /**
   * Invoked whenever a configuration method succeeded.
   */
  void onConfigurationSuccess(ITestResult itr);

  /**
   * Invoked whenever a configuration method failed.
   */
  void onConfigurationFailure(ITestResult itr);

  /**
   * Invoked whenever a configuration method was skipped.
   */
  void onConfigurationSkip(ITestResult itr);
}

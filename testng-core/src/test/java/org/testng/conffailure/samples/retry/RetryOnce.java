package org.testng.conffailure.samples.retry;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/** Retries a failed test method once. */
public class RetryOnce implements IRetryAnalyzer {

  private int retries;

  @Override
  public boolean retry(ITestResult result) {
    return retries++ < 1;
  }
}

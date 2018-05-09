package org.testng.internal.annotations;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.Test;

/**
 * A No operation retry analyzer that exists just to let us use proper types in @{@link
 * Test#retryAnalyzer()}
 */
public class DisabledRetryAnalyzer implements IRetryAnalyzer {
  @Override
  public boolean retry(ITestResult result) {
    return false;
  }
}

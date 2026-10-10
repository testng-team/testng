package org.testng.listeners.samples;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Records the test context TestNG starts, and the one it puts on a result. A test compares the two.
 */
public class ResultContextListener implements ITestListener {

  public static ITestContext contextStarted;
  public static ITestContext contextOnResult;

  @Override
  public void onStart(ITestContext context) {
    contextStarted = context;
  }

  @Override
  public void onTestStart(ITestResult result) {
    contextOnResult = result.getTestContext();
  }
}

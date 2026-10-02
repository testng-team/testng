package org.testng.listeners.samples;

import org.testng.ITestListener;
import org.testng.ITestResult;

public class BaseListener implements ITestListener {

  @Override
  public void onTestStart(ITestResult result) {
    AggregateSampleTest.incrementCount();
  }
}

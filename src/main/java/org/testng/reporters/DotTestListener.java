package org.testng.reporters;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class DotTestListener extends TestListenerAdapter {
  private int m_count = 0;

  @Override
  public void onTestFailure(ITestResult tr) {
    log("F");
  }

  @Override
  public void onTestSkipped(ITestResult tr) {
    log("S");
  }

  @Override
  public void onTestSuccess(ITestResult tr) {
    log(".");
  }

  private void log(String string) {
    System.out.print(string);
    if (m_count++ % 40 == 0) {
      System.out.println("");
    }
  }

}

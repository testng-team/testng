package test.listeners;

import org.testng.ITestResult;
import org.testng.internal.IResultListener2;

public class ResultListener implements IResultListener2 {

  public static long m_end = 0;

  @Override
  public void onTestSuccess(ITestResult result) {
    m_end = result.getEndMillis();
  }
}

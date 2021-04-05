package test.listeners;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class L3 extends TestListenerAdapter {
  @Override
  public void onTestStart(ITestResult result) {
    BaseWithListener.incrementCount();
  }
}

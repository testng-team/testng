package test.listeners.github1393;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class Listener1393 extends TestListenerAdapter {

  public void onTestStart(ITestResult testContext) {
    super.onTestStart(testContext);
    System.out.println("In onTestStart");
    testContext.setStatus(ITestResult.FAILURE);
    throw new RuntimeException("Trying to fail a test");
  }
}

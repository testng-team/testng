package test.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ResultContextListener implements ITestListener {

  public static boolean contextProvided = false;

  public void onTestStart(ITestResult result) {
    ITestContext context = result.getTestContext();
    if (context != null) {
      contextProvided = true;
    }
  }
}

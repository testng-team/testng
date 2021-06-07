package test.listeners.github1130;

import java.util.ArrayList;
import java.util.List;
import org.testng.IClassListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestResult;

public class MyListener implements ISuiteListener, IClassListener {

  public static int count = 0;
  public static List<String> beforeSuiteCount = new ArrayList<>();
  public static List<String> beforeClassCount = new ArrayList<>();

  public MyListener() {
    count++;
  }

  public void onStart(ISuite suite) {
    beforeSuiteCount.add(this.toString());
  }

  public void onBeforeClass(ITestClass testClass) {
    beforeClassCount.add(this.toString());
  }

  public void onTestStart(ITestResult result) {}

  public void onTestSuccess(ITestResult result) {}

  public void onTestFailure(ITestResult result) {}

  public void onTestSkipped(ITestResult result) {}

  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}

  public void onStart(ITestContext context) {}

  public void onFinish(ITestContext context) {}

  public void onFinish(ISuite suite) {}

  public void onAfterClass(ITestClass testClass) {}
}

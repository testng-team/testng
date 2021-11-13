package test.attributes.issue2346;

import java.util.HashMap;
import java.util.Map;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class LocalTestListener implements ITestListener {
  public static Map<String, Boolean> data = new HashMap<>();

  public void onTestStart(ITestResult iTestResult) {
    String key = "onTestStart_" + iTestResult.getMethod().getQualifiedName();
    data.putIfAbsent(key, iTestResult.getAttributeNames().isEmpty());
  }

  public void onTestSuccess(ITestResult iTestResult) {
    String key = "onTestSuccess_" + iTestResult.getMethod().getQualifiedName();
    data.putIfAbsent(key, iTestResult.getAttributeNames().isEmpty());
  }

  public void onTestFailure(ITestResult iTestResult) {
    String key = "onTestFailure_" + iTestResult.getMethod().getQualifiedName();
    data.putIfAbsent(key, iTestResult.getAttributeNames().isEmpty());
  }

  public void onTestSkipped(ITestResult iTestResult) {
    String key = "onTestSkipped_" + iTestResult.getMethod().getQualifiedName();
    data.putIfAbsent(key, iTestResult.getAttributeNames().isEmpty());
  }
}

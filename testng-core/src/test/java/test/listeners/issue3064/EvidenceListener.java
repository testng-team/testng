package test.listeners.issue3064;

import org.testng.ITestListener;
import org.testng.ITestResult;

public class EvidenceListener implements ITestListener {
  public static final String ATTRIBUTE_KEY = "attributeKey";
  public static ITestResult failureTestResult;

  @Override
  public void onTestStart(ITestResult result) {
    result.setAttribute(ATTRIBUTE_KEY, "attributeValue");
  }

  @Override
  public void onTestFailure(ITestResult result) {
    failureTestResult = result;
  }
}

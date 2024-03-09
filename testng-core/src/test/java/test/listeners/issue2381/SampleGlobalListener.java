package test.listeners.issue2381;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.IAlterSuiteListener;
import org.testng.IClassListener;
import org.testng.IExecutionListener;
import org.testng.IExecutionVisualiser;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

public class SampleGlobalListener
    implements IExecutionListener,
        IAlterSuiteListener,
        ISuiteListener,
        ITestListener,
        IInvokedMethodListener,
        IClassListener,
        IExecutionVisualiser,
        IReporter {

  private static final List<String> logs = new ArrayList<>();

  public static List<String> getLogs() {
    return Collections.unmodifiableList(logs);
  }

  public static void clearLogs() {
    logs.clear();
  }

  @Override
  public void onExecutionStart() {
    logs.add("onExecutionStart");
  }

  @Override
  public void alter(List<XmlSuite> suites) {
    logs.add("alter");
  }

  @Override
  public void onStart(ISuite suite) {
    logs.add("onStart(ISuite)");
  }

  @Override
  public void onStart(ITestContext context) {
    logs.add("onStart(ITestContext)");
  }

  @Override
  public void onBeforeClass(ITestClass testClass) {
    logs.add("onBeforeClass");
  }

  @Override
  public void consumeDotDefinition(String dotDefinition) {
    logs.add("consumeDotDefinition");
  }

  @Override
  public void onTestStart(ITestResult result) {
    logs.add("onTestStart(ITestResult)");
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    logs.add("beforeInvocation");
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    logs.add("afterInvocation");
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    logs.add("onTestSuccess(ITestResult)");
  }

  @Override
  public void onTestFailure(ITestResult result) {
    logs.add("onTestFailure(ITestResult)");
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    logs.add("onTestSkipped(ITestResult)");
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    logs.add("onTestFailedButWithinSuccessPercentage(ITestResult)");
  }

  @Override
  public void onTestFailedWithTimeout(ITestResult result) {
    logs.add("onTestFailedWithTimeout(ITestResult)");
  }

  @Override
  public void onAfterClass(ITestClass testClass) {
    logs.add("onAfterClass");
  }

  @Override
  public void onFinish(ITestContext context) {
    logs.add("onFinish(ITestContext)");
  }

  @Override
  public void onFinish(ISuite suite) {
    logs.add("onFinish(ISuite)");
  }

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    logs.add("generateReport");
  }

  @Override
  public void onExecutionFinish() {
    logs.add("onExecutionFinish");
  }

  @Override
  public boolean isEnabled() {
    return false;
  }
}

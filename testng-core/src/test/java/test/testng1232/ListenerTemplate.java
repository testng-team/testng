package test.testng1232;

import java.util.List;
import org.testng.*;
import org.testng.xml.XmlSuite;

/**
 * This class provides "void" implementations for all listener invocations so that one can tweak
 * behavior of only those methods which need customization. (Mainly to circumvent verbosity in
 * actual listener implementations)
 */
public class ListenerTemplate
    implements IInvokedMethodListener,
        IClassListener,
        ITestListener,
        ISuiteListener,
        IAlterSuiteListener,
        IExecutionListener,
        IReporter {

  @Override
  public void onBeforeClass(ITestClass testClass) {}

  @Override
  public void onAfterClass(ITestClass testClass) {}

  @Override
  public void onStart(ISuite suite) {}

  @Override
  public void onFinish(ISuite suite) {}

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {}

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {}

  @Override
  public void onTestStart(ITestResult result) {}

  @Override
  public void onTestSuccess(ITestResult result) {}

  @Override
  public void onTestFailure(ITestResult result) {}

  @Override
  public void onTestSkipped(ITestResult result) {}

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}

  @Override
  public void onStart(ITestContext context) {}

  @Override
  public void onFinish(ITestContext context) {}

  @Override
  public void onExecutionStart() {}

  @Override
  public void onExecutionFinish() {}

  @Override
  public void alter(List<XmlSuite> suites) {}

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {}
}

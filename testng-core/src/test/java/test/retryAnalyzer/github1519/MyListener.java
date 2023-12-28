package test.retryAnalyzer.github1519;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class MyListener implements IInvokedMethodListener {

  @Override
  public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
    TestClassSample.messages.add("afterInvocation");
  }
}

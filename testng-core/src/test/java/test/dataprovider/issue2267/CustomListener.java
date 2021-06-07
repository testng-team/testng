package test.dataprovider.issue2267;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.internal.annotations.DisabledRetryAnalyzer;
import test.dataprovider.DataProviderRetryAnalyzer;

public class CustomListener implements IInvokedMethodListener {

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {

    if (method.isTestMethod()) {

      final IRetryAnalyzer currentRetryAnalyzer =
          method.getTestMethod().getRetryAnalyzer(testResult);

      if (currentRetryAnalyzer == null || currentRetryAnalyzer instanceof DisabledRetryAnalyzer) {
        method.getTestMethod().setRetryAnalyzerClass(DataProviderRetryAnalyzer.class);
      }
    }
  }
}

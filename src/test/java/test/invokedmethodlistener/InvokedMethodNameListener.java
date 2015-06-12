package test.invokedmethodlistener;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.util.HashSet;
import java.util.Set;

public class InvokedMethodNameListener implements IInvokedMethodListener {

  final Set<String> testMethods = new HashSet<>();
  final Set<String> configurationMethods = new HashSet<>();
  final Set<String> testMethodsFromTM = new HashSet<>();
  final Set<String> configurationMethodsFromTM = new HashSet<>();

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    String methodName = method.getTestMethod().getMethodName();

    if (method.isTestMethod()) {
      testMethods.add(methodName);
    }
    if (method.isConfigurationMethod()) {
      configurationMethods.add(methodName);
    }
    if (method.getTestMethod().isTest()) {
      testMethodsFromTM.add(methodName);
    }
    if (method.getTestMethod().isBeforeMethodConfiguration() ||
        method.getTestMethod().isAfterMethodConfiguration() ||
        method.getTestMethod().isBeforeTestConfiguration() ||
        method.getTestMethod().isAfterTestConfiguration() ||
        method.getTestMethod().isBeforeClassConfiguration() ||
        method.getTestMethod().isAfterClassConfiguration() ||
        method.getTestMethod().isBeforeSuiteConfiguration() ||
        method.getTestMethod().isAfterSuiteConfiguration()) {
      configurationMethodsFromTM.add(methodName);
    }
  }
}

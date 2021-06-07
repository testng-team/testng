package org.testng.internal.invokers;

import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.internal.ConfigurationGroupMethods;
import org.testng.internal.ITestResultNotifier;
import org.testng.xml.XmlSuite;

public interface ITestInvoker {

  class FailureContext {

    int count = 0;
    List<Object> instances = Lists.newArrayList();
    boolean representsRetriedMethod = false;
  }

  List<ITestResult> invokeTestMethods(
      ITestNGMethod testMethod,
      ConfigurationGroupMethods groupMethods,
      Object instance,
      ITestContext context);

  ITestResult invokeTestMethod(
      TestMethodArguments arguments, XmlSuite suite, FailureContext failureContext);

  FailureContext retryFailed(
      TestMethodArguments arguments,
      List<ITestResult> result,
      int failureCount,
      ITestContext testContext);

  void runTestResultListener(ITestResult tr);

  default ITestResult registerSkippedTestResult(
      ITestNGMethod testMethod, long start, Throwable throwable) {
    return registerSkippedTestResult(testMethod, start, throwable, null);
  }

  ITestResult registerSkippedTestResult(
      ITestNGMethod testMethod, long start, Throwable throwable, ITestResult source);

  void invokeListenersForSkippedTestResult(ITestResult r, IInvokedMethod invokedMethod);

  ITestResultNotifier getNotifier();

  default IMethodRunner getRunner() {
    return new MethodRunner();
  }
}

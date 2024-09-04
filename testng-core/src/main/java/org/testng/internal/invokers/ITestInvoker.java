package org.testng.internal.invokers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

    AtomicInteger count = new AtomicInteger(0);
    List<Object> instances = Lists.newArrayList();
    AtomicBoolean representsRetriedMethod = new AtomicBoolean(false);
    final Map<String, AtomicInteger> counter = new HashMap<>();
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

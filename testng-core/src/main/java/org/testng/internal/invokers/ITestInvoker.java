package org.testng.internal.invokers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.Nullable;
import org.testng.IInvokedMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.ConfigurationGroupMethods;
import org.testng.internal.IConfiguration;
import org.testng.internal.ITestResultNotifier;
import org.testng.xml.XmlSuite;

public interface ITestInvoker {

  class FailureContext {

    AtomicInteger count = new AtomicInteger(0);
    List<Object> instances = new ArrayList<>();
    AtomicBoolean representsRetriedMethod = new AtomicBoolean(false);
    final Map<String, AtomicInteger> counter = new HashMap<>();
  }

  List<ITestResult> invokeTestMethods(
      ITestNGMethod testMethod,
      ConfigurationGroupMethods groupMethods,
      @Nullable Object instance,
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
      ITestNGMethod testMethod, long start, @Nullable Throwable throwable) {
    return registerSkippedTestResult(testMethod, start, throwable, null);
  }

  /**
   * @param source the result to copy attributes and parameters from, or null when the skip has no
   *     originating result
   */
  ITestResult registerSkippedTestResult(
      ITestNGMethod testMethod,
      long start,
      @Nullable Throwable throwable,
      @Nullable ITestResult source);

  void invokeListenersForSkippedTestResult(ITestResult r, IInvokedMethod invokedMethod);

  ITestResultNotifier getNotifier();

  IConfiguration getConfiguration();

  default IMethodRunner getRunner() {
    return new MethodRunner();
  }
}

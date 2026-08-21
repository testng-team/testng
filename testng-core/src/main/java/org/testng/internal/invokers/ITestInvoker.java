package org.testng.internal.invokers;

import java.util.ArrayList;
import java.util.Collections;
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

  /** For an invocation nothing was ever resolved for, so there are no values to report it with. */
  default ITestResult registerSkippedTestResult(
      ITestNGMethod testMethod, long start, @Nullable Throwable throwable) {
    return registerSkippedTestResult(testMethod, start, throwable, new Object[0]);
  }

  /**
   * Registers the result of an invocation that will not run, and announces it as starting.
   *
   * <p>The values are assigned before the listeners are told, so that a listener reading {@link
   * ITestResult#getParameters()} from {@code onTestStart} already sees what the result will be
   * reported with.
   *
   * @param parameterValues the values the invocation would have run with, empty when nothing was
   *     ever resolved for it
   */
  ITestResult registerSkippedTestResult(
      ITestNGMethod testMethod,
      long start,
      @Nullable Throwable throwable,
      Object[] parameterValues);

  /**
   * The same, for a skip standing in for an invocation that was already built: it takes both its
   * values and its attributes, so the two cannot be given separately and disagree.
   */
  ITestResult registerSkippedTestResult(
      ITestNGMethod testMethod, long start, @Nullable Throwable throwable, ITestResult source);

  void invokeListenersForSkippedTestResult(ITestResult r, IInvokedMethod invokedMethod);

  /**
   * Cancels the {@code invocationCount}s an invocation leaves behind by failing, when {@code
   * skipFailedInvocationCounts} or {@code skipFailedInvocations} asks for it.
   *
   * <p>The one place that happens, so a cancelled invocation is announced and reported the same way
   * whether its data provider is parallel or not. Both callers -- {@link
   * IMethodRunner#runInSequence} and {@link TestMethodWithDataProviderMethodWorker} -- are
   * cancelling the same thing, down to the counter.
   *
   * @param remaining the counter the invocation loop is driven from, shared by every row: draining
   *     it is what stops the counts still to come from running, so a caller that finds it drained
   *     cancels nothing -- whoever got there first has already accounted for them
   * @param parameterValues the row the cancelled invocations would have re-run, which is the row
   *     the failed one ran with
   * @return one result per cancelled invocation, empty when there is nothing to cancel
   */
  default List<ITestResult> cancelRemainingInvocations(
      ITestNGMethod testMethod,
      AtomicInteger remaining,
      int failureCount,
      boolean skipFailedInvocationCounts,
      Object[] parameterValues,
      long start) {
    if (failureCount <= 0 || !(skipFailedInvocationCounts || testMethod.skipFailedInvocations())) {
      return Collections.emptyList();
    }
    List<ITestResult> cancelled = new ArrayList<>();
    while (remaining.getAndDecrement() > 0) {
      cancelled.add(registerCancelledInvocation(testMethod, start, parameterValues));
    }
    return cancelled;
  }

  private ITestResult registerCancelledInvocation(
      ITestNGMethod testMethod, long start, Object[] parameterValues) {
    ITestResult result = registerSkippedTestResult(testMethod, start, null, parameterValues);
    // The notifier is what fills ITestContext, and so what the built-in reporters are generated
    // from. Neither caller's return value reaches it.
    getNotifier().addSkippedTest(testMethod, result);
    invokeListenersForSkippedTestResult(
        result, new InvokedMethod(System.currentTimeMillis(), result));
    return result;
  }

  ITestResultNotifier getNotifier();

  IConfiguration getConfiguration();

  default IMethodRunner getRunner() {
    return new MethodRunner();
  }
}

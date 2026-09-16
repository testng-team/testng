package org.testng.internal.invokers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestNGException;
import org.testng.internal.ConfigurationGroupMethods;
import org.testng.xml.XmlSuite;

public class TestMethodWithDataProviderMethodWorker
    implements Callable<List<ITestResult>>, Comparable<TestMethodWithDataProviderMethodWorker> {

  private final ITestNGMethod m_testMethod;

  /**
   * Built on the worker's own thread, in {@link #call()}, not on the thread that schedules the
   * rows: a resolver may bind what it creates to the thread it runs on, and the invocation must be
   * the one that gets it.
   */
  private final Supplier<Object[]> m_parameterValues;

  /** The row as the provider gave it, for the failure that names it when it does not fit. */
  private final Object[] m_row;

  private final Object m_instance;
  private final Map<String, String> m_parameters;
  private final ITestClass m_testClass;
  private final ITestNGMethod[] m_beforeMethods;
  private final ITestNGMethod[] m_afterMethods;
  private final ConfigurationGroupMethods m_groupMethods;
  private final ITestContext m_testContext;
  private int m_parameterIndex;
  private final boolean m_skipFailedInvocationCounts;
  private final AtomicInteger m_invocationCount;
  private final ITestInvoker m_testInvoker;

  private final List<ITestResult> m_testResults = new ArrayList<>();
  private int m_failureCount;

  public TestMethodWithDataProviderMethodWorker(
      ITestInvoker testInvoker,
      ITestNGMethod testMethod,
      int parameterIndex,
      Object[] row,
      Supplier<Object[]> parameterValues,
      Object instance,
      Map<String, String> parameters,
      ITestClass testClass,
      ITestNGMethod[] beforeMethods,
      ITestNGMethod[] afterMethods,
      ConfigurationGroupMethods groupMethods,
      ITestContext testContext,
      boolean skipFailedInvocationCounts,
      AtomicInteger invocationCount,
      int failureCount) {
    this.m_testInvoker = testInvoker;
    m_testMethod = testMethod;
    m_parameterIndex = parameterIndex;
    m_row = row;
    m_parameterValues = parameterValues;
    m_instance = instance;
    m_parameters = parameters;
    m_testClass = testClass;
    m_beforeMethods = beforeMethods;
    m_afterMethods = afterMethods;
    m_groupMethods = groupMethods;
    m_skipFailedInvocationCounts = skipFailedInvocationCounts;
    m_testContext = testContext;
    m_invocationCount = invocationCount;
    m_failureCount = failureCount;
  }

  @Override
  public List<ITestResult> call() {
    List<ITestResult> tmpResults = new ArrayList<>();
    long start = System.currentTimeMillis();
    Object[] parameterValues;
    try {
      parameterValues = m_parameterValues.get();
    } catch (TestNGException rowDoesNotFit) {
      // Thrown here, on the worker, it would come back through join() wrapped in a
      // CompletionException. Reported here instead, it is this row's own failure, and the
      // rows on the other workers still run.
      m_testResults.add(m_testInvoker.failRowThatDoesNotFit(m_testMethod, m_row, rowDoesNotFit));
      return m_testResults;
    }
    XmlSuite suite = m_testContext.getSuite().getXmlSuite();

    final ITestInvoker.FailureContext failure = new ITestInvoker.FailureContext();
    failure.count.set(m_failureCount);
    try {
      tmpResults.add(
          m_testInvoker.invokeTestMethod(
              new TestMethodArguments.Builder()
                  .usingInstance(m_instance)
                  .forTestMethod(m_testMethod)
                  .withParameterValues(parameterValues)
                  .withParametersIndex(m_parameterIndex)
                  .withParameters(m_parameters)
                  .forTestClass(m_testClass)
                  .usingBeforeMethods(m_beforeMethods)
                  .usingAfterMethods(m_afterMethods)
                  .usingGroupMethods(m_groupMethods)
                  .build(),
              suite,
              failure));
    } finally {
      m_failureCount = failure.count.get();
      if (failure.instances.isEmpty()) {
        m_testResults.addAll(tmpResults);
      } else {
        for (Object instance : failure.instances) {
          List<ITestResult> retryResults = new ArrayList<>();

          m_failureCount =
              m_testInvoker
                  .retryFailed(
                      new TestMethodArguments.Builder()
                          .usingInstance(instance)
                          .forTestMethod(m_testMethod)
                          .withParameterValues(parameterValues)
                          .withParametersIndex(m_parameterIndex)
                          .withParameters(m_parameters)
                          .forTestClass(m_testClass)
                          .usingBeforeMethods(m_beforeMethods)
                          .usingAfterMethods(m_afterMethods)
                          .usingGroupMethods(m_groupMethods)
                          .build(),
                      retryResults,
                      m_failureCount,
                      m_testContext)
                  .count
                  .get();
          m_testResults.addAll(retryResults);
        }
      }

      //
      // If we have a failure, skip all the
      // other invocationCounts
      //
      m_testResults.addAll(
          m_testInvoker.cancelRemainingInvocations(
              m_testMethod,
              m_invocationCount,
              m_failureCount,
              m_skipFailedInvocationCounts,
              parameterValues,
              start));
    }
    m_parameterIndex++;

    return m_testResults;
  }

  @Override
  public int compareTo(TestMethodWithDataProviderMethodWorker o) {
    return Integer.compare(this.m_testMethod.getPriority(), o.m_testMethod.getPriority());
  }
}

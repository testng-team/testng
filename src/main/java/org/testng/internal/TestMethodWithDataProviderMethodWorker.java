package org.testng.internal;

import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class TestMethodWithDataProviderMethodWorker implements Callable<List<ITestResult>> {

  private final ITestNGMethod m_testMethod;
  private final Object[] m_parameterValues;
  private final Object m_instance;
  private final Map<String, String> m_parameters;
  private final ITestClass m_testClass;
  private final ITestNGMethod[] m_beforeMethods;
  private final ITestNGMethod[] m_afterMethods;
  private final ConfigurationGroupMethods m_groupMethods;
  private final Invoker m_invoker;
  private final ITestContext m_testContext;
  private int m_parameterIndex;
  private boolean m_skipFailedInvocationCounts;
  private int m_invocationCount;
  private final ITestResultNotifier m_notifier;

  private final List<ITestResult> m_testResults = Lists.newArrayList();
  private int m_failureCount;

  public TestMethodWithDataProviderMethodWorker(
      Invoker invoker,
      ITestNGMethod testMethod,
      int parameterIndex,
      Object[] parameterValues,
      Object instance,
      Map<String, String> parameters,
      ITestClass testClass,
      ITestNGMethod[] beforeMethods,
      ITestNGMethod[] afterMethods,
      ConfigurationGroupMethods groupMethods,
      ITestContext testContext,
      boolean skipFailedInvocationCounts,
      int invocationCount,
      int failureCount,
      ITestResultNotifier notifier) {
    m_invoker = invoker;
    m_testMethod = testMethod;
    m_parameterIndex = parameterIndex;
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
    m_notifier = notifier;
  }

  @Override
  public List<ITestResult> call() {
    List<ITestResult> tmpResults = Lists.newArrayList();
    long start = System.currentTimeMillis();
    XmlSuite suite = m_testContext.getSuite().getXmlSuite();

    final Invoker.FailureContext failure = new Invoker.FailureContext();
    failure.count = m_failureCount;
    try {
      tmpResults.add(
          m_invoker.invokeTestMethod(
              m_instance,
              m_testMethod,
              m_parameterValues,
              m_parameterIndex,
              suite,
              m_parameters,
              m_testClass,
              m_beforeMethods,
              m_afterMethods,
              m_groupMethods,
              failure));
    } finally {
      m_failureCount = failure.count;
      if (failure.instances.isEmpty()) {
        m_testResults.addAll(tmpResults);
      } else {
        for (Object instance : failure.instances) {
          List<ITestResult> retryResults = Lists.newArrayList();

          m_failureCount =
              m_invoker.retryFailed(
                      instance,
                      m_testMethod,
                      m_parameterValues,
                      m_testClass,
                      m_beforeMethods,
                      m_afterMethods,
                      m_groupMethods,
                      retryResults,
                      m_failureCount,
                      m_testContext,
                      m_parameters,
                      m_parameterIndex)
                  .count;
          m_testResults.addAll(retryResults);
        }
      }

      //
      // If we have a failure, skip all the
      // other invocationCounts
      //

      // If not specified globally, use the attribute
      // on the annotation
      //
      if (!m_skipFailedInvocationCounts) {
        m_skipFailedInvocationCounts = m_testMethod.skipFailedInvocations();
      }
      if (m_failureCount > 0 && m_skipFailedInvocationCounts) {
        while (m_invocationCount-- > 0) {
          ITestResult r =
              TestResult.newEndTimeAwareTestResult(m_testMethod, m_testContext, null, start);
          r.setStatus(TestResult.SKIP);
          m_testResults.add(r);
          m_invoker.runTestResultListener(r);
          m_notifier.addSkippedTest(m_testMethod, r);
        }
      }
    }
    m_parameterIndex++;

    return m_testResults;
  }

  public int getInvocationCount() {
    return m_invocationCount;
  }
}

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

  private ITestNGMethod m_testMethod;
  private Object[] m_parameterValues;
  private Object m_instance;
  private XmlSuite m_xmlSuite;
  private Map<String, String> m_parameters;
  private ITestClass m_testClass;
  private ITestNGMethod[] m_beforeMethods;
  private ITestNGMethod[] m_afterMethods;
  private ConfigurationGroupMethods m_groupMethods;
  private Invoker m_invoker;
  private ExpectedExceptionsHolder m_expectedExceptionHolder;
  private ITestContext m_testContext;
  private int m_parameterIndex;
  private boolean m_skipFailedInvocationCounts;
  private int m_invocationCount;
  private ITestResultNotifier m_notifier;

  private List<ITestResult> m_testResults = Lists.newArrayList();
  private int m_failureCount;

  public TestMethodWithDataProviderMethodWorker(Invoker invoker, ITestNGMethod testMethod,
      int parameterIndex,
      Object[] parameterValues, Object instance, XmlSuite suite,
      Map<String, String> parameters, ITestClass testClass,
      ITestNGMethod[] beforeMethods, ITestNGMethod[] afterMethods,
      ConfigurationGroupMethods groupMethods, ExpectedExceptionsHolder expectedExceptionHolder,
      ITestContext testContext, boolean skipFailedInvocationCounts,
      int invocationCount, int failureCount, ITestResultNotifier notifier) {
    m_invoker = invoker;
    m_testMethod = testMethod;
    m_parameterIndex = parameterIndex;
    m_parameterValues = parameterValues;
    m_instance = instance;
    m_xmlSuite = suite;
    m_parameters = parameters;
    m_testClass = testClass;
    m_beforeMethods = beforeMethods;
    m_afterMethods = afterMethods;
    m_groupMethods = groupMethods;
    m_expectedExceptionHolder = expectedExceptionHolder;
    m_skipFailedInvocationCounts = skipFailedInvocationCounts;
    m_testContext = testContext;
    m_invocationCount = invocationCount;
    m_failureCount = failureCount;
    m_notifier = notifier;
  }

  public long getMaxTimeOut() {
    return 500;
  }

  @Override
  public List<ITestResult> call() {
    List<ITestResult> tmpResults = Lists.newArrayList();
    long start = System.currentTimeMillis();

    final Invoker.FailureContext failure = new Invoker.FailureContext();
    failure.count = m_failureCount;
    try {
      tmpResults.add(m_invoker.invokeTestMethod(m_instance,
          m_testMethod,
          m_parameterValues,
          m_parameterIndex,
          m_xmlSuite,
          m_parameters,
          m_testClass,
          m_beforeMethods,
          m_afterMethods,
          m_groupMethods,
          failure));
    }
    finally {
      m_failureCount = failure.count;
      if (failure.instances.isEmpty()) {
        m_testResults.addAll(tmpResults);
      } else {
        for (Object instance : failure.instances) {
          List<ITestResult> retryResults = Lists.newArrayList();

          m_failureCount =
             m_invoker.retryFailed(
                 instance, m_testMethod, m_xmlSuite, m_testClass, m_beforeMethods,
                 m_afterMethods, m_groupMethods, retryResults,
                 m_failureCount, m_expectedExceptionHolder,
                 m_testContext, m_parameters, m_parameterIndex);
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
      if (! m_skipFailedInvocationCounts) {
        m_skipFailedInvocationCounts = m_testMethod.skipFailedInvocations();
      }
      if (m_failureCount > 0 && m_skipFailedInvocationCounts) {
        while (m_invocationCount-- > 0) {
          ITestResult r =
            new TestResult(m_testMethod.getTestClass(),
              m_instance,
              m_testMethod,
              null,
              start,
              System.currentTimeMillis(),
              m_testContext);
          r.setStatus(TestResult.SKIP);
          m_testResults.add(r);
          m_invoker.runTestListeners(r);
          m_notifier.addSkippedTest(m_testMethod, r);
        }
      }
    }
    m_parameterIndex++;

    return m_testResults;
  }

  public List<ITestResult> getTestResults() {
    return m_testResults;
  }

  public int getInvocationCount() {
    return m_invocationCount;
  }

  public int getFailureCount() {
    return m_failureCount;
  }
}

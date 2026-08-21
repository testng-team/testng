package org.testng.internal.invokers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.ConfigurationGroupMethods;
import org.testng.internal.invokers.TestMethodArguments.Builder;
import org.testng.xml.XmlSuite;

public class TestMethodWithDataProviderMethodWorker
    implements Callable<List<ITestResult>>, Comparable<TestMethodWithDataProviderMethodWorker> {

  private final ITestNGMethod m_testMethod;
  private final Object[] m_parameterValues;
  private final Object m_instance;
  private final Map<String, String> m_parameters;
  private final ITestClass m_testClass;
  private final ITestNGMethod[] m_beforeMethods;
  private final ITestNGMethod[] m_afterMethods;
  private final ConfigurationGroupMethods m_groupMethods;
  private final ITestContext m_testContext;
  private int m_parameterIndex;
  private boolean m_skipFailedInvocationCounts;
  private int m_invocationCount;
  private final ITestInvoker m_testInvoker;

  private final List<ITestResult> m_testResults = new ArrayList<>();
  private int m_failureCount;

  public TestMethodWithDataProviderMethodWorker(
      ITestInvoker testInvoker,
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
      int failureCount) {
    this.m_testInvoker = testInvoker;
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
  }

  @Override
  public List<ITestResult> call() {
    List<ITestResult> tmpResults = new ArrayList<>();
    long start = System.currentTimeMillis();
    XmlSuite suite = m_testContext.getSuite().getXmlSuite();

    final ITestInvoker.FailureContext failure = new ITestInvoker.FailureContext();
    failure.count.set(m_failureCount);
    try {
      tmpResults.add(
          m_testInvoker.invokeTestMethod(
              new Builder()
                  .usingInstance(m_instance)
                  .forTestMethod(m_testMethod)
                  .withParameterValues(m_parameterValues)
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
                      new Builder()
                          .usingInstance(instance)
                          .forTestMethod(m_testMethod)
                          .withParameterValues(m_parameterValues)
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

      // If not specified globally, use the attribute
      // on the annotation
      //
      if (!m_skipFailedInvocationCounts) {
        m_skipFailedInvocationCounts = m_testMethod.skipFailedInvocations();
      }
      if (m_failureCount > 0 && m_skipFailedInvocationCounts) {
        while (m_invocationCount-- > 0) {
          m_testResults.add(
              m_testInvoker.registerCancelledInvocation(m_testMethod, start, m_parameterValues));
        }
      }
    }
    m_parameterIndex++;

    return m_testResults;
  }

  public int getInvocationCount() {
    return m_invocationCount;
  }

  @Override
  public int compareTo(TestMethodWithDataProviderMethodWorker o) {
    return Integer.compare(this.m_testMethod.getPriority(), o.m_testMethod.getPriority());
  }
}

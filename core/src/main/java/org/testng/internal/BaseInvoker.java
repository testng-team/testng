package org.testng.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.SuiteRunState;
import org.testng.collections.Maps;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.invokers.InvokedMethodListenerInvoker;
import org.testng.internal.invokers.InvokedMethodListenerMethod;

class BaseInvoker {

  private final Collection<IInvokedMethodListener> m_invokedMethodListeners;
  protected final ITestResultNotifier m_notifier;
  protected final ITestContext m_testContext;
  protected final SuiteRunState m_suiteState;
  protected IConfiguration m_configuration;

  /** Class failures must be synced as the Invoker is accessed concurrently */
  protected final Map<Class<?>, Set<Object>> m_classInvocationResults = Maps.newConcurrentMap();

  public BaseInvoker(ITestResultNotifier notifier,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      ITestContext testContext, SuiteRunState suiteState,
      IConfiguration configuration) {
    this.m_notifier = notifier;
    this.m_invokedMethodListeners = invokedMethodListeners;
    this.m_testContext = testContext;
    this.m_suiteState = suiteState;
    this.m_configuration = configuration;
  }

  protected IAnnotationFinder annotationFinder() {
    return m_configuration.getAnnotationFinder();
  }

  protected void runInvokedMethodListeners(
      InvokedMethodListenerMethod listenerMethod,
      IInvokedMethod invokedMethod,
      ITestResult testResult) {
    if (noListenersPresent()) {
      return;
    }

    InvokedMethodListenerInvoker invoker =
        new InvokedMethodListenerInvoker(listenerMethod, testResult, m_testContext);
    for (IInvokedMethodListener currentListener : m_invokedMethodListeners) {
      invoker.invokeListener(currentListener, invokedMethod);
    }
  }

  private boolean noListenersPresent() {
    return (m_invokedMethodListeners == null) || (m_invokedMethodListeners.isEmpty());
  }

  /**
   * An exception was thrown by the test, determine if this method should be marked as a failure or
   * as failure_but_within_successPercentage
   */
  protected void handleException(
      Throwable throwable, ITestNGMethod testMethod, ITestResult testResult, int failureCount) {
    if (throwable != null && testResult.getThrowable() == null) {
      testResult.setThrowable(throwable);
    }
    int successPercentage = testMethod.getSuccessPercentage();
    int invocationCount = testMethod.getInvocationCount();
    float numberOfTestsThatCanFail = ((100 - successPercentage) * invocationCount) / 100f;

    if (failureCount < numberOfTestsThatCanFail) {
      testResult.setStatus(ITestResult.SUCCESS_PERCENTAGE_FAILURE);
    } else {
      testResult.setStatus(ITestResult.FAILURE);
    }
  }

  protected boolean isSkipExceptionAndSkip(Throwable ite) {
    return SkipException.class.isAssignableFrom(ite.getClass()) && ((SkipException) ite).isSkip();
  }

  static void log(int level, String s) {
    Utils.log("Invoker " + Thread.currentThread().hashCode(), level, s);
  }

}

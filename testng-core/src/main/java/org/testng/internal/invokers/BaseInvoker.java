package org.testng.internal.invokers;

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
import org.testng.SuiteRunner;
import org.testng.collections.Maps;
import org.testng.internal.IConfiguration;
import org.testng.internal.ITestResultNotifier;
import org.testng.internal.ListenerOrderDeterminer;
import org.testng.internal.Utils;
import org.testng.internal.annotations.IAnnotationFinder;

class BaseInvoker {

  private final Collection<IInvokedMethodListener> m_invokedMethodListeners;
  protected final ITestResultNotifier m_notifier;
  protected final ITestContext m_testContext;
  protected final SuiteRunState m_suiteState;
  protected IConfiguration m_configuration;

  /** Class failures must be synced as the Invoker is accessed concurrently */
  protected final Map<Class<?>, Set<Object>> m_classInvocationResults = Maps.newConcurrentMap();

  // This object essentially represents the instance to which a BeforeTest|AfterTest
  // method belongs to. Currently TestNG handles this with a null value.
  // Instead we now would be using this special object.
  protected final Object NULL_OBJECT = new Object();

  private final SuiteRunner suiteRunner;

  public BaseInvoker(
      ITestResultNotifier notifier,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      ITestContext testContext,
      SuiteRunState suiteState,
      IConfiguration configuration,
      SuiteRunner suiteRunner) {
    this.m_notifier = notifier;
    this.m_invokedMethodListeners = invokedMethodListeners;
    this.m_testContext = testContext;
    this.m_suiteState = suiteState;
    this.m_configuration = configuration;
    this.suiteRunner = suiteRunner;
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
        new InvokedMethodListenerInvoker(listenerMethod, testResult, testResult.getTestContext());
    // For BEFORE_INVOCATION method, still run as insert order, but regarding AFTER_INVOCATION, it
    // should be reverse order
    boolean isAfterInvocation = InvokedMethodListenerMethod.AFTER_INVOCATION == listenerMethod;
    Collection<IInvokedMethodListener> listeners =
        isAfterInvocation
            ? ListenerOrderDeterminer.reversedOrder(m_invokedMethodListeners)
            : ListenerOrderDeterminer.order(m_invokedMethodListeners);
    if (!isAfterInvocation) {
      suiteRunner.beforeInvocation(invokedMethod, testResult);
    }
    for (IInvokedMethodListener currentListener : listeners) {
      try {
        invoker.invokeListener(currentListener, invokedMethod);
      } catch (SkipException e) {
        String msg =
            String.format(
                "Caught a [%s] exception from one of listeners %s. Will mark [%s()] as SKIPPED.",
                SkipException.class.getSimpleName(),
                currentListener.getClass().getName(),
                invokedMethod.getTestMethod().getQualifiedName());
        Utils.warn(msg);
        testResult.setStatus(ITestResult.SKIP);
        testResult.setThrowable(e);
      }
    }
    if (isAfterInvocation) {
      suiteRunner.afterInvocation(invokedMethod, testResult);
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

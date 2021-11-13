package org.testng.internal.invokers;

import static org.testng.internal.invokers.InvokedMethodListenerMethod.AFTER_INVOCATION;
import static org.testng.internal.invokers.InvokedMethodListenerMethod.BEFORE_INVOCATION;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestResult;

/**
 * Hides complexity of calling methods of {@link IInvokedMethodListener}.
 *
 * @author Ansgar Konermann
 */
public class InvokedMethodListenerInvoker {

  private final InvokedMethodListenerMethod m_listenerMethod;
  private final ITestContext m_testContext;
  private final ITestResult m_testResult;

  /**
   * Creates a new invoker instance which can be used to call the specified {@code listenerMethod}
   * on any number of {@link IInvokedMethodListener}s.
   *
   * @param listenerMethod method which should be called.
   * @param testResult test result which should be passed to the listener method upon invocation.
   * @param testContext test context which should be passed to the listener method upon invocation.
   *     This parameter is only used when calling methods on an {@link IInvokedMethodListener}.
   */
  public InvokedMethodListenerInvoker(
      InvokedMethodListenerMethod listenerMethod,
      ITestResult testResult,
      ITestContext testContext) {
    m_listenerMethod = listenerMethod;
    m_testContext = testContext;
    m_testResult = testResult;
  }

  /**
   * Invokes the given {@code listenerInstance}, calling the method specified in the constructor of
   * this {@link InvokedMethodListenerInvoker}.
   *
   * @param listenerInstance the listener instance which should be invoked.
   * @param invokedMethod the {@link IInvokedMethod} instance which should be passed to the {@link
   *     IInvokedMethodListener#beforeInvocation(IInvokedMethod, ITestResult)}, {@link
   *     IInvokedMethodListener#afterInvocation(IInvokedMethod, ITestResult)}, {@link
   *     IInvokedMethodListener#beforeInvocation(IInvokedMethod, ITestResult, ITestContext)} or
   *     {@link IInvokedMethodListener#afterInvocation(IInvokedMethod, ITestResult, ITestContext)}
   *     method.
   */
  public void invokeListener(
      IInvokedMethodListener listenerInstance, IInvokedMethod invokedMethod) {
    if (this.m_listenerMethod == BEFORE_INVOCATION) {
      listenerInstance.beforeInvocation(invokedMethod, m_testResult);
      listenerInstance.beforeInvocation(invokedMethod, m_testResult, m_testContext);
    }
    if (this.m_listenerMethod == AFTER_INVOCATION) {
      listenerInstance.afterInvocation(invokedMethod, m_testResult);
      listenerInstance.afterInvocation(invokedMethod, m_testResult, m_testContext);
    }
  }
}

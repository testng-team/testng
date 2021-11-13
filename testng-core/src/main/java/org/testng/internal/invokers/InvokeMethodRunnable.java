package org.testng.internal.invokers;

import java.util.concurrent.Callable;
import org.testng.IHookable;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.ConstructorOrMethod;

/** A Runnable Method invoker. */
public class InvokeMethodRunnable implements Callable<Void> {
  private final ITestNGMethod m_method;
  private final Object m_instance;
  private final Object[] m_parameters;
  private final IHookable m_hookable;
  private final ITestResult m_testResult;

  public InvokeMethodRunnable(
      ITestNGMethod thisMethod,
      Object instance,
      Object[] parameters,
      IHookable hookable,
      ITestResult testResult) {
    m_method = thisMethod;
    m_instance = instance;
    m_parameters = parameters;
    m_hookable = hookable;
    m_testResult = testResult;
  }

  public void run() throws TestNGRuntimeException {
    try {
      call();
    } catch (Exception e) {
      throw new TestNGRuntimeException(e);
    }
  }

  private void runOne() {
    try {
      RuntimeException t = null;
      try {
        ConstructorOrMethod m = m_method.getConstructorOrMethod();
        if (m_hookable == null) {
          MethodInvocationHelper.invokeMethod(m.getMethod(), m_instance, m_parameters);
        } else {
          MethodInvocationHelper.invokeHookable(
              m_instance, m_parameters, m_hookable, m.getMethod(), m_testResult);
        }
      } catch (Throwable e) {
        Throwable cause = e.getCause();
        if (cause == null) {
          cause = e;
        }
        t = new TestNGRuntimeException(cause);
      }
      if (null != t) {
        Thread.currentThread().interrupt();
        throw t;
      }
    } finally {
      m_method.incrementCurrentInvocationCount();
    }
  }

  @Override
  public Void call() throws Exception {
    if (m_method.getInvocationTimeOut() > 0) {
      for (int i = 0; i < m_method.getInvocationCount(); i++) {
        runOne();
      }
    } else {
      runOne();
    }
    return null;
  }

  public static class TestNGRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -8619899270785596231L;

    public TestNGRuntimeException(Throwable rootCause) {
      super(rootCause);
    }
  }
}

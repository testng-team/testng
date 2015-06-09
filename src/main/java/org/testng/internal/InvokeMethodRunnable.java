package org.testng.internal;

import org.testng.IHookable;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A Runnable Method invoker.
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>the_mindstorm</a>
 */
public class InvokeMethodRunnable implements Runnable {
  private ITestNGMethod m_method = null;
  private Object m_instance = null;
  private Object[] m_parameters = null;
  private final IHookable hookable;
  private final ITestResult testResult;

  public InvokeMethodRunnable(ITestNGMethod thisMethod,
                              Object instance,
                              Object[] parameters,
                              IHookable hookable,
                              ITestResult testResult)
  {
    m_method = thisMethod;
    m_instance = instance;
    m_parameters = parameters;
    this.hookable = hookable;
    this.testResult = testResult;
  }

  @Override
  public void run() throws TestNGRuntimeException {
    // If there is an invocation time out, all the invocations need to be done within this
    // Runnable
    if (m_method.getInvocationTimeOut() > 0) {
      for (int i = 0; i < m_method.getInvocationCount(); i++) {
        runOne();
      }
    }
    else {
      runOne();
    }
  }

  private void runOne() {
    try {
      RuntimeException t = null;
      try {
        Method m = m_method.getMethod();
        if (hookable == null) {
          MethodInvocationHelper.invokeMethod(m, m_instance, m_parameters);
        } else {
          MethodInvocationHelper.invokeHookable(m_instance, m_parameters, hookable, m, testResult);
        }
      }
      catch(Throwable e) {
        t = new TestNGRuntimeException(e.getCause());
      }
      if(null != t) {
        Thread.currentThread().interrupt();
        throw t;
      }
    }
    finally {
      m_method.incrementCurrentInvocationCount();
    }
  }

  public static class TestNGRuntimeException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = -8619899270785596231L;

    public TestNGRuntimeException(Throwable rootCause) {
      super(rootCause);
    }
  }
}

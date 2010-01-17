package org.testng.internal;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.testng.ITestNGMethod;
import org.testng.internal.thread.ICountDown;

/**
 * A Runnable Method invoker.
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>the_mindstorm</a>
 */
public class InvokeMethodRunnable implements Runnable {
  private ITestNGMethod m_method = null;
  private Object m_instance = null;
  private Object[] m_parameters = null;

  public InvokeMethodRunnable(ITestNGMethod thisMethod,
                              Object instance,
                              Object[] parameters) 
  {
    m_method = thisMethod;
    m_instance = instance;
    m_parameters = parameters;
  }

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
        MethodHelper.invokeMethod(m, m_instance, m_parameters);
      }
      catch(InvocationTargetException e) {
        t = new TestNGRuntimeException(e.getCause());
      }
      catch(IllegalAccessException e) {
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
    public TestNGRuntimeException(Throwable rootCause) {
      super(rootCause);
    }
  }
}

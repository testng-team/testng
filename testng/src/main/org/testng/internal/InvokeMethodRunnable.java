package org.testng.internal;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.testng.internal.thread.ICountDown;

/**
 * A Runnable Method invoker.
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>the_mindstorm</a>
 */
public class InvokeMethodRunnable implements Runnable {
  private Method     m_method = null;
  private Object     m_instance = null;
  private Object[]   m_parameters = null;
  private ICountDown m_done = null;
  private List<String> m_output = new ArrayList<String>();

  public InvokeMethodRunnable(Method thisMethod,
                              Object instance,
                              Object[] parameters,
                              ICountDown done)
//                              List<String> output) 
  {
    m_method = thisMethod;
    m_instance = instance;
    m_parameters = parameters;
    m_done = done;
//    m_output = output;
  }

  public void run() throws TestNGRuntimeException {
    try {
      RuntimeException t = null;
      try {
        MethodHelper.invokeMethod(m_method, m_instance, m_parameters);
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
      m_done.countDown();
    }
  }
  
  public static class TestNGRuntimeException extends RuntimeException {
    public TestNGRuntimeException(Throwable rootCause) {
      super(rootCause);
    }
  }
}

package org.testng.internal.invokers;

import org.testng.IInvokedMethodListener;
import org.testng.IInvokedMethodListener2;
import org.testng.TestNGException;

public enum InvokedMethodListenerSubtype {

  EXTENDED_LISTENER(IInvokedMethodListener2.class),
  SIMPLE_LISTENER(IInvokedMethodListener.class);

  private Class<? extends IInvokedMethodListener> m_matchingInterface;

  private InvokedMethodListenerSubtype(Class<? extends IInvokedMethodListener> listenerClass) {
    m_matchingInterface = listenerClass;
  }

  private boolean isInstance(IInvokedMethodListener listenerInstance) {
    return m_matchingInterface.isInstance(listenerInstance);
  }

  public static InvokedMethodListenerSubtype fromListener(IInvokedMethodListener listenerInstance) {
    if (EXTENDED_LISTENER.isInstance(listenerInstance)) {
      return EXTENDED_LISTENER;
    }
    else if (SIMPLE_LISTENER.isInstance(listenerInstance)) {
      return SIMPLE_LISTENER;
    }
    throw new TestNGException("Illegal " + IInvokedMethodListener.class.getSimpleName()
        + " instance: " + listenerInstance.getClass().getName() + ".");
  }
}

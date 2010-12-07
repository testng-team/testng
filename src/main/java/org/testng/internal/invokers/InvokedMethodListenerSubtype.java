package org.testng.internal.invokers;

import org.testng.IInvokedMethodListener;
import org.testng.IInvokedMethodListener2;
import org.testng.TestNGException;

public enum InvokedMethodListenerSubtype {

  SIMPLE_LISTENER(IInvokedMethodListener.class),
  EXTENDED_LISTENER(IInvokedMethodListener2.class);

  private Class<? extends IInvokedMethodListener> matchingInterface;

  private InvokedMethodListenerSubtype(Class<? extends IInvokedMethodListener> listenerClass) {
    this.matchingInterface = listenerClass;
  }

  private boolean isInstance(IInvokedMethodListener listenerInstance) {
    return matchingInterface.isInstance(listenerInstance);
  }

  public static InvokedMethodListenerSubtype fromListener(IInvokedMethodListener listenerInstance) {
    if (EXTENDED_LISTENER.isInstance(listenerInstance)) {
      return EXTENDED_LISTENER;
    }
    else if (SIMPLE_LISTENER.isInstance(listenerInstance)) {
      return SIMPLE_LISTENER;
    }
    throw new TestNGException("Illegal " + IInvokedMethodListener.class.getSimpleName() + " instance: " + listenerInstance.getClass().getName() + ".");
  }
}

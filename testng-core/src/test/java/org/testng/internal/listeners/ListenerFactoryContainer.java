package org.testng.internal.listeners;

import org.testng.IExecutionListener;
import org.testng.ITestNGListener;
import org.testng.ITestNGListenerFactory;

public class ListenerFactoryContainer {
  public static class DummyListenerFactory implements ITestNGListenerFactory, IExecutionListener {
    @Override
    public ITestNGListener createListener(Class<? extends ITestNGListener> listenerClass) {
      return this;
    }
  }

  public static class DummyListenerFactory2 implements ITestNGListenerFactory, IExecutionListener {
    @Override
    public ITestNGListener createListener(Class<? extends ITestNGListener> listenerClass) {
      return this;
    }
  }
}

package org.testng.internal;

import org.testng.ITestNGListener;
import org.testng.ITestNGListenerFactory;
import org.testng.ITestObjectFactory;

/**
 * When no {@link ITestNGListenerFactory} implementations are available, TestNG defaults to this
 * implementation for instantiating listeners.
 */
public final class DefaultListenerFactory implements ITestNGListenerFactory {

  private final ITestObjectFactory m_objectFactory;

  public DefaultListenerFactory(ITestObjectFactory objectFactory) {
    this.m_objectFactory = objectFactory;
  }

  @Override
  public ITestNGListener createListener(Class<? extends ITestNGListener> listenerClass) {
    return m_objectFactory.newInstance(listenerClass);
  }
}

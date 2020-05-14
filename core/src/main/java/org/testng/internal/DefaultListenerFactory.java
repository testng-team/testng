package org.testng.internal;

import org.testng.ITestNGListener;
import org.testng.ITestNGListenerFactory;

/**
 * When no {@link ITestNGListenerFactory} implementations are available, TestNG defaults to this
 * implementation for instantiating listeners.
 */
public final class DefaultListenerFactory implements ITestNGListenerFactory {
  @Override
  public ITestNGListener createListener(Class<? extends ITestNGListener> listenerClass) {
    return InstanceCreator.newInstance(listenerClass);
  }
}

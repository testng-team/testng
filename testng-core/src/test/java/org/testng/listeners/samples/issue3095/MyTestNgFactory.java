package org.testng.listeners.samples.issue3095;

import org.testng.ITestNGListener;
import org.testng.ITestNGListenerFactory;

public class MyTestNgFactory implements ITestNGListener, ITestNGListenerFactory {
  @Override
  public ITestNGListener createListener(Class<? extends ITestNGListener> listenerClass) {
    return null;
  }
}

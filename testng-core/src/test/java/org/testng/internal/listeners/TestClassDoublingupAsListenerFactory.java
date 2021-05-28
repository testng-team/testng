package org.testng.internal.listeners;

import org.testng.ITestNGListener;
import org.testng.ITestNGListenerFactory;
import org.testng.annotations.Test;

public class TestClassDoublingupAsListenerFactory implements ITestNGListenerFactory {
  @Override
  public ITestNGListener createListener(Class<? extends ITestNGListener> listenerClass) {
    return new EmptyExecutionListener();
  }

  @Test
  public void test() {}
}

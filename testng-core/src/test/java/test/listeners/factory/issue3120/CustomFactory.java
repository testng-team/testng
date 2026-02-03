package test.listeners.factory.issue3120;

import org.testng.IExecutionListener;
import org.testng.ITestNGListener;
import org.testng.ITestNGListenerFactory;

public class CustomFactory implements IExecutionListener, ITestNGListenerFactory {

  public static boolean factoryInvoked = false;
  public static boolean listenerInvoked = false;

  @Override
  public ITestNGListener createListener(Class<? extends ITestNGListener> aClass) {
    factoryInvoked = true;
    return this;
  }

  @Override
  public void onExecutionStart() {
    listenerInvoked = true;
  }
}

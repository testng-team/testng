package test.listeners.factory;

import org.testng.ITestNGListener;
import org.testng.ITestNGListenerFactory;
import org.testng.internal.objects.InstanceCreator;

public class SampleTestFactory implements ITestNGListenerFactory {

  public static ITestNGListenerFactory instance;

  private boolean invoked = false;

  public boolean isInvoked() {
    return invoked;
  }

  public SampleTestFactory() {
    setInstance(this);
  }

  private static void setInstance(ITestNGListenerFactory instance) {
    SampleTestFactory.instance = instance;
  }

  @Override
  public ITestNGListener createListener(Class<? extends ITestNGListener> listenerClass) {
    invoked = true;
    return InstanceCreator.newInstance(listenerClass);
  }
}

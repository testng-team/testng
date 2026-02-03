package test.listeners.factory.issue3120;

import org.testng.Assert;
import org.testng.IExecutionListener;
import org.testng.ITestNGListener;
import org.testng.ITestNGListenerFactory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(FactoryListenerTestClassCombinedSample.class)
public class FactoryListenerTestClassCombinedSample
    implements IExecutionListener, ITestNGListenerFactory {

  private static boolean factoryInvoked = false;
  private static boolean listenerInvoked = false;

  @Override
  public ITestNGListener createListener(Class<? extends ITestNGListener> listenerClass) {
    factoryInvoked = true;
    return this;
  }

  @Override
  public void onExecutionStart() {
    listenerInvoked = true;
  }

  @Test
  public void sampleTestMethod() {
    Assert.assertTrue(factoryInvoked, "Factory should have been invoked");
    Assert.assertTrue(listenerInvoked, "Listener should have been invoked");
  }
}

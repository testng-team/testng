package test.listeners.factory.issue3120;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(CustomFactory.class)
public class TestClassSample {

  @Test
  public void sampleTestMethod() {
    Assert.assertTrue(CustomFactory.factoryInvoked, "Factory should have been invoked");
    Assert.assertTrue(CustomFactory.listenerInvoked, "Listener should have been invoked");
  }
}

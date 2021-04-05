package test.github1490;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import test.listeners.github1490.LocalDataProviderListener;

@Listeners(LocalDataProviderListener.class)
public class FactoryPoweredDataProviderWithListenerAnnotationSample {
  private final int i;

  @Factory(dataProvider = "dp")
  public FactoryPoweredDataProviderWithListenerAnnotationSample(int i) {
    this.i = i;
  }

  @Test
  public void testMethod() {
    Assert.assertTrue(i > 0);
  }

  @DataProvider(name = "dp")
  public static Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}

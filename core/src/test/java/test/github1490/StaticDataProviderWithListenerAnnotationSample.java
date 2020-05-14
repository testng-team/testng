package test.github1490;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import test.listeners.github1490.DataProviderInfoProvider;

@Listeners(DataProviderInfoProvider.class)
public class StaticDataProviderWithListenerAnnotationSample {
  private int i;

  @Factory(dataProvider = "getStaticData")
  public StaticDataProviderWithListenerAnnotationSample(int i) {
    this.i = i;
  }

  @Test
  public void testMethod() {
    Assert.assertTrue(i > 0);
  }

  @DataProvider
  public static Object[][] getStaticData() {
    return new Object[][] {{1}, {2}};
  }
}

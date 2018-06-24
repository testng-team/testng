package test.github1490;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SimpleDataProviderWithoutListenerAnnotationSample {
  @Test(dataProvider = "getData")
  public void testMethod(int i) {
    Assert.assertTrue(i > 0);
  }

  @DataProvider
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}

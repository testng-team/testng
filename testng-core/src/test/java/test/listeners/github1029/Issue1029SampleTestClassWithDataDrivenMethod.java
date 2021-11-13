package test.listeners.github1029;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Issue1029SampleTestClassWithDataDrivenMethod {
  @Test(dataProvider = "dp")
  public void a(int i) {
    Assert.assertTrue(i > 0);
  }

  @DataProvider(name = "dp", parallel = true)
  public Object[][] getData() {
    return new Object[][] {{1}, {2}, {3}, {4}, {5}};
  }
}

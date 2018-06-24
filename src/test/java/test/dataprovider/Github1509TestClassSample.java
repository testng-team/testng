package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Github1509TestClassSample {
  @Test(dataProvider = "dp")
  public void demo(int i) {
    Assert.assertTrue(i > 0);
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return null;
  }
}

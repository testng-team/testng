package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InstanceDataProviderSample {

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {{hashCode()}};
  }

  @Test(dataProvider = "dp")
  public void f(Integer n) {
    Assert.assertEquals(n, Integer.valueOf(hashCode()));
  }
}

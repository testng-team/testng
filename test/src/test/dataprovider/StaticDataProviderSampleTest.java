package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StaticDataProviderSampleTest {

  @Test(dataProvider = "static", dataProviderClass = StaticProvider.class)
  public void verifyStatic(String s) {
    Assert.assertEquals(s, "Cedric");
  }
}

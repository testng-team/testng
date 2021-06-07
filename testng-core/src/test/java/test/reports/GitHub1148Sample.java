package test.reports;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GitHub1148Sample {

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {{"Cedric"}, {"Anne"}};
  }

  @Test(dataProvider = "dp")
  public void verifyData(String n1) {
    Assert.assertEquals(n1, "Cedric");
  }
}

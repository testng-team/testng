package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnnamedDataProviderSample {

  @Test(dataProvider = "unnamedDataProvider")
  public void doStuff(boolean t) {}

  @DataProvider
  public Object[][] unnamedDataProvider() {
    return new Object[][] {{Boolean.TRUE}, {Boolean.FALSE}};
  }
}

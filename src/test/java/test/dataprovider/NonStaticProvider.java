package test.dataprovider;

import org.testng.annotations.DataProvider;

public class NonStaticProvider {

  @DataProvider(name = "external")
  public Object[][] create() {
    return new Object[][] {
        new Object[] { "Cedric" },
    };
  }
}

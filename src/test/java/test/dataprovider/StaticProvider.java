package test.dataprovider;

import org.testng.annotations.DataProvider;

public class StaticProvider {

  @DataProvider(name = "static")
  public static Object[][] create() {
    return new Object[][] {
        new Object[] { "Cedric" },
    };
  }
}

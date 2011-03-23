package test.factory;

import org.testng.annotations.DataProvider;

public class DPClass {
  @DataProvider
  static public Object[][] dp() {
    return new Object[][] {
      new Object[] { 43 },
      new Object[] { 44 },
    };
  }

}

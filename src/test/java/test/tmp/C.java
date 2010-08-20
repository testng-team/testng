package test.tmp;

import org.testng.annotations.DataProvider;

public class C {
  @DataProvider(name = "data")
  static public Object[][] data() { 
    return new Object[][] {
        new Object[] { "Foo" },
        new Object[] { "Bar" },
    };
  }
}

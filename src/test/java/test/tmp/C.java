package test.tmp;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

public class C {
  @BeforeMethod
  public void cm() {
    System.out.println("C.cm");
  }

  @DataProvider(name = "data")
  static public Object[][] data() { 
    return new Object[][] {
        new Object[] { "Foo" },
        new Object[] { "Bar" },
    };
  }
}

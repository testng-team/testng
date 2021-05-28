package test.tmp;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class C {
  @BeforeMethod
  public void cm() {
//    System.out.println("C.cm");
  }

  @Test(dataProvider = "data")
  public void c(String s) {
    System.out.println("c(" + s + ")");
  }

  @DataProvider(name = "data")
  static public Object[][] data() {
    return new Object[][] {
        new Object[] { "Foo" },
        new Object[] { "Bar" },
    };
  }
}

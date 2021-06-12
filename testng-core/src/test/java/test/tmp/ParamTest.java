package test.tmp;

import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ParamTest {

  @Test(dataProvider = "dp")
  public void f(String s, int n) {
    //    if ("Alois".equals(s)) assert false;
    Reporter.log("CALL " + n);
  }

  @DataProvider(name = "dp")
  public Object[][] create() {
    return new Object[][] {
      new Object[] {"Cedric", 36},
      new Object[] {"Alois", 35},
    };
  }
}

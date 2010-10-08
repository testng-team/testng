package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(dataProvider = "dp")
public class ClassSampleTest {

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
        new Object[] { "a" },
        new Object[] { "b" },
    };
  }

  public void f(String a) {
  }

  public void g(String a) {
  }

}

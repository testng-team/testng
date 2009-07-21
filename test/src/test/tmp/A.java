package test.tmp;

import org.testng.ITest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(dataProvider = "dp")
public class A implements ITest {

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

  public String getTestName() {
    return "Test name";
  }
}

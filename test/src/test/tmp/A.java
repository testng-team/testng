package test.tmp;

import org.testng.ITest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class A implements ITest {
  
  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
        new Object[] { "a" },
        new Object[] { "b" },
    };
  }

  @Test(groups = "mytest", dependsOnMethods = "g")
  public void f() {
  }

  @Test
  public void g() {
  }

  @AfterMethod
  public void after() {
  }

  public String getTestName() {
    return "Test name";
  }
}

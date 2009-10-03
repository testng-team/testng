package test.tmp;

import org.testng.ITest;
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

  @Test
  (threadPoolSize = 2)
  public void g() {
  }

  //  @Test(groups = "mytest", dependsOnMethods = "g")
//  public void f() {
//  }
//
//
//  @AfterMethod
//  public void after() {
//  }

  public String getTestName() {
    return "Test name";
  }
}

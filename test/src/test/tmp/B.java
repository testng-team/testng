package test.tmp;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class B {
  
  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
        new Object[] { "a" },
        new Object[] { "b" },
    };
  }

  @BeforeClass
  public void beforeClassB() {
    
  }

  @Test
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

}

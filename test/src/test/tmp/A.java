package test.tmp;

import org.testng.ITest;
import org.testng.annotations.BeforeClass;
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

  @BeforeClass
  public void beforeClassA() {
    
  }

  @Test
  public void g1() {
  }

  @Test
  public void g2() {
  }

  public String getTestName() {
    return "This is test A";
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

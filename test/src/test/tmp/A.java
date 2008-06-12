package test.tmp;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class A {
  
  @BeforeMethod
  public void before() {
    System.out.println("BEFORE");
  }
  
  @DataProvider
  public Object[][] dp() {
    return new Object[][] { 
        new Object[] { "a" },
        new Object[] { "b" },
    };
  }

  @Test(dataProvider = "dp")
  public void f(String a) {
    System.out.println("TEST:" + a);
  }
  
}

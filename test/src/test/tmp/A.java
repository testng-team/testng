package test.tmp;

import org.testng.ITest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class A implements ITest {
  
  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
        new Object[] { "10.10.10" },
        new Object[] { "10.10" },
    };
  }

//  @Test(dataProvider = "dp")
//  public void verifyIPAddress(String ip) {
//    System.out.println("IP:" + ip);
//  }

  @BeforeClass
  public void beforeClassA() {
  }

  @AfterClass
  public void afterClassA() {
  }

  @Test
  public void g1() {
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

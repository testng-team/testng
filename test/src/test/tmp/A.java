package test.tmp;

import org.testng.ITest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

public class A implements ITest {
  
  public static void main(String[] args) {
    for (int i = 0; i < 10; i++) {
      try {
        System.out.println("i:" + i);
        if (i == 3) break;
      }
      finally {
        System.out.println("finally");
      }
    }
    System.out.println("outside");
  }

  @DataProvider
  public Object[][] dp() {
    return new Object[][] { 
      new Object[] { 1 },  
      new Object[] { 2 },  
      new Object[] { 3},  
    };
  }

  @Test(dataProvider = "dp")
  public void verifyIPAddress(Integer ip) {
    if (ip == 2) throw new RuntimeException();
  }

//  @BeforeSuite
//  public void bs() {
//    System.out.println("Before suite");
//  }
//
//  @AfterSuite
//  public void as() {
//    System.out.println("After suite");
//  }
//
//  @BeforeClass
//  public void beforeClassA() {
//  }
//
//  @AfterClass
//  public void afterClassA() {
//  }
//
//  @Test
//  public void g1() {
////    throw new RuntimeException();
//  }


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

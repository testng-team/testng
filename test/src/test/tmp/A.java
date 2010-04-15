package test.tmp;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

@Test
public class A {

  private void log(String s) {
    System.out.println(hashCode() + " [A]" + s);
  }

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      new Object[] { 42 },   
    };
  }

//  @BeforeClass
//  public void bc() {
//    log("beforeClass");
//  }
//
//  @AfterClass
//  public void ac() {
//    log("afterClass");
//  }

//  @Test(dataProvider = "dp")
  public void f1(Integer n) {
    log("f1");
  }

  @Test(expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "incorrect argument")
  public void f2() {
    throw new IllegalArgumentException("val1 is incorrect");
  }

  //  @Test
//  public void f2() {
//    log("f2");
//  }
//  @Test(priority = 1)
//  public void f1() {
//  }
//
//  @Test(priority = -1)
//  public void f2() {
//  }
//
//  @Test
//  public void f3() {
//  }

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

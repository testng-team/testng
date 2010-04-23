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

  @Test(priority = 1)
  public void ctest1() {
  }

  @Test(priority = 2, groups = "group1")
  public void btest2() {
  }

  @Test(priority = 3)
  public void atest3() {
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

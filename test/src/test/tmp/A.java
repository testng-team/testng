package test.tmp;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Parameters({"n"})
@Test
public class A {

  public A() {
    log("A");
  }

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

  @BeforeMethod
  public void bm() {
    log("BeforeMethod");
//    throw new RuntimeException();
  }

  @AfterMethod
  public void am() {
    log("AfterMethod");
  }

  @Test(dataProvider = "dp")
  public void f1(Integer n) {
    log("f1");
  }

  public void f2() {
    log("f2");
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

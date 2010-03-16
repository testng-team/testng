package test.tmp;

import org.testng.ITest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Parameters({"n"})
@Test
public class A {


  private void log(String s) {
    System.out.println("[A]" + s);
  }

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      new Object[] { 42 },   
    };
  }
//  @Override
//  public boolean equals(Object other) {
//    if (other == null) return false;
//    if (other == this) return true;
//    return ((A) other).m_n == m_n;
//  }

//  @BeforeClass
//  public void bc() {
//    log("beforeClass");
//  }
//
//  @AfterClass
//  public void ac() {
//    log("afterClass");
//  }

  public void f1(Integer n) {
    log("f1 " + n);
  }

  public void f2(Integer n) {
    log("f1 " + n);
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

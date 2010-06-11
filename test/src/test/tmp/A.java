package test.tmp;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

@Test(sequential = true)
public class A {
  private int m_n;

  public A() {}

  public A(int n) {
    m_n = n;
  }

  private void log(String s) {
    System.out.println(" [A(" + m_n + ") thread:" + Thread.currentThread().getId() + "] " + s);
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

  @Factory
  public Object[] create() {
    return new Object[] { new A(1), new A(2) };
  }

  @Test
  public void ctest1() {
    log("ctest1");
  }

  @Test
  public void btest2() {
    log("btest2");
  }

//  @Test(priority = 3)
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

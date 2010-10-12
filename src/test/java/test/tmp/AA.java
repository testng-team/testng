package test.tmp;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

@Test(sequential = true)
public class AA {
  private int m_n;

  public AA() {}

  public AA(int n) {
    m_n = n;
  }

  private void log(String s) {
    System.out.println(" [AA(" + m_n + ") thread:" + Thread.currentThread().getId() + "] " + s);
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
    return new Object[] { new A(), new AA() };
  }

  @Test
  public void aatest1() {
    log("aatest1");
  }

  @Test(dependsOnMethods = "aatest1")
  public void aatest2() {
    log("aatest2");
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

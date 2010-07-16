package test.tmp;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

//@Test(sequential = true)
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

  @Test
  public void testSomething()
  {
      Assert.fail("This test failed.");
  }

  @Test(dependsOnMethods={"testSomething"})
  public void testSkipped()
  {
      Reporter.log("This test was skipped.");
  }

  @Test
  public void atest1() {
    try {
      Thread.sleep(12*1000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

//  @Test(dependsOnMethods = "atest1")
  public void atest2() {
    log("atest2");
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

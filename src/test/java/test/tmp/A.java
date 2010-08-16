package test.tmp;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import test.SimpleBaseTest;
import test.listeners.ResultListener;

//@Test(sequential = true)
@Listeners(ResultListener.class)
public class A extends SimpleBaseTest {
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

  @BeforeClass
  public void beforeClass() {
//    throw new RuntimeException();
  }

//  @AfterClass
//  public void afterClass(ITestResult tr) {
//    System.out.println("Result:" + tr.getEndMillis());
//  }

  @Test
  public void atest1() {
    try {
      Thread.sleep(1*1000);
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

//  @Override
  public String getTestName() {
    return "This is test A";
  }

//  @Test(groups = "mytest", dependsOnMethods = "g")
//  public void f() {
//  }
//
//  @AfterClass
//  public void ac() {
//    log("afterClass");
//  }

  public static void main(String[] args) {
//    TestNG tng = create();
  }


}

package test.tmp;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITest;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class A implements ITest {
  
  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
        new Object[] { "a" },
        new Object[] { "b" },
    };
  }

  @Test(dataProvider = "dp")
  public void f(String s) {
  }

  @AfterClass
  public void afterClass() {
    p("After class");
  }
  
  public void p(String s) {
    System.out.println(Thread.currentThread().getId() + " " + s);
  }
  
  public static void main(String[] args) {
    TestNG tng = new TestNG();
    IInvokedMethodListener l = new IInvokedMethodListener() {

      public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        System.out.println("Done invoking " + method);
      }

      public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        System.out.println("About to invoke " + method
            + "\n annotation:" + method.getTestMethod().getMethod().getAnnotations()[0]);
      }
      
    };
    tng.addListener(l);
    tng.setTestClasses(new Class[] { A.class });
    tng.run();
  }
//  public String toString() {
//    return "[A: msg:" + msg + "]";
//  }

  public String getTestName() {
    return "Placeholder";
  }
}

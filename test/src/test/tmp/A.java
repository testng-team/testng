package test.tmp;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class A {
  
  @Test
  public void f() {
    TestNG tng = new TestNG();
    tng.setTestClasses(new Class[] { Test_TestListenerAppender.class});
    TestListenerAdapter tla = new TestListenerAdapter() {
      @Override
      public void onTestStart(ITestResult tr) {
        System.out.println("START");
        super.onTestStart(tr);
      }
      
      @Override
      public void onTestSuccess(ITestResult tr) {
        System.out.println("SUCCESS");
        super.onTestSuccess(tr);
      }

      @Override
      public void onTestSkipped(ITestResult tr) {
        System.out.println("SKIPPED");
        super.onTestSkipped(tr);
      }
    };
    tng.addListener(tla);
    
    tng.run();
  }
}

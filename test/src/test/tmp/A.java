package test.tmp;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class A {
  
  String msg;

//  public A(String ech) {
//    msg = ech;
//  }

  @Test
  public void testEcho(ITestContext c) {
    c.getSuite().setAttribute("test", 42);
    System.out.println("A Context:");
    throw new SkipException("Skipped");
  }
  
  @AfterMethod
  public void after(ITestContext c, ITestResult result) {
    System.out.println(c.getPassedConfigurations() + " result:" + result);
  }
  
  public void p(String s) {
    System.out.println(Thread.currentThread().getId() + " " + s);
  }
  
  public String toString() {
    return "[A: msg:" + msg + "]";
  }
}

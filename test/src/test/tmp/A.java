package test.tmp;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class A {
  
  String msg;

  public A(String ech) {
    msg = ech;
  }

  @Test
  public void testEcho() {
    System.out.println("echo: " + msg);
    if ("def".equals(msg)) throw new RuntimeException();
  }

  
  public void p(String s) {
    System.out.println(Thread.currentThread().getId() + " " + s);
  }
  
  public String toString() {
    return "[A: msg:" + msg + "]";
  }
}

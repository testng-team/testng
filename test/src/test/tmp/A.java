package test.tmp;

import org.testng.annotations.Test;

public class A {
  
//  @Test(expectedExceptions = RuntimeException.class)
//  public void method1() {
//    System.out.println("In method1");
//    throw new Error("");
//  }
//   
//  @Test(expectedExceptions = RuntimeException.class)
//  public void method2() {
//    System.out.println("In method2");
//    throw new RuntimeException("");
//  }
   
  @Test(expectedExceptions = RuntimeException.class)
  public void method3() {
    System.out.println("In method3");
  }

  
  public void p(String s) {
    System.out.println(Thread.currentThread().getId() + " " + s);
  }
  
//  public String toString() {
//    return "[A: msg:" + msg + "]";
//  }
}

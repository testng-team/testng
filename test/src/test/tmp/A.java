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
   
  @Test(timeOut = 2000)
  public void method3() {
    System.out.println("Before loop");
    for (int j = 0; j < 1000000000; j++) {
      for (int i = 0; i < 1000000000; i++) {
        for (int k = 0; k < 1000000000; k++) {
          
        }
      }
    }
    System.out.println("After loop");
  }

  
  public void p(String s) {
    System.out.println(Thread.currentThread().getId() + " " + s);
  }
  
//  public String toString() {
//    return "[A: msg:" + msg + "]";
//  }
}

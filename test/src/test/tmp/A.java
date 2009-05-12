package test.tmp;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class A {
  private List mList;
  
  public boolean putIfAbsent(List l, Object o) {
    boolean absent = true;
    synchronized(l) {
      absent = ! l.contains(o);
      if (absent) {
        l.add(o);
      }
    }
    return absent;
  }
  
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

  @BeforeMethod
  public void init() {
    mList = new ArrayList();
  }

  @Test(invocationCount = 100, threadPoolSize = 5)
  public void method3() {
    Integer n = new Integer(42);
    Assert.assertEquals(mList.size(), 0);
    boolean absent = putIfAbsent(mList, n);
    Assert.assertTrue(absent);
    Assert.assertEquals(mList.size(), 1);
    boolean absent2 = putIfAbsent(mList, n);
    Assert.assertFalse(absent2);
    Assert.assertEquals(mList.size(), 1);
  }

  
  public void p(String s) {
    System.out.println(Thread.currentThread().getId() + " " + s);
  }
  
//  public String toString() {
//    return "[A: msg:" + msg + "]";
//  }
}

package test.tmp;

import org.testng.annotations.BeforeMethod;

public class Base {
  @BeforeMethod
  public void bm() {
      System.out.println("Thread" + Thread.currentThread().getId());
  }
}
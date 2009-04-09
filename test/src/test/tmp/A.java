package test.tmp;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Random;

public class A {
  
  @DataProvider
  public Object[][] dp2() {
    return new Object[][] {
        new Object[] { 11 },
        new Object[] { 12 },
        new Object[] { 13 },
        new Object[] { 14 },
    };
  }

  @Test(dataProvider = "dp2")
  public void f2(Integer n) {
    sleep("f2", n);
  }
  
  private void sleep(String f, Integer n) {
    System.out.println("Thread:" + Thread.currentThread().getId() + " " + f + "(" + n + ")");
    try {
      Thread.sleep(Math.abs(new Random().nextLong() % 2000));
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
        new Object[] { 1 },
        new Object[] { 2 },
        new Object[] { 3 },
        new Object[] { 4 },
    };
  }

  @Test(dataProvider = "dp")
  public void f(Integer n) {
    sleep("f", n);
  }

}

package test.tmp;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Random;

@Test
public class A {

  public void g1() { log("g1"); }
  public void g2() { log("g2"); }
  public void g3() { log("g3"); }
  public void g4() { log("g4"); }
  
  @DataProvider(parallel = true)
  public Object[][] dp1() {
    return new Object[][] {
        new Object[] { 1 },
        new Object[] { 2 },
        new Object[] { 3 },
        new Object[] { 4 },
    };
  }

  @Test(dataProvider = "dp1")
  public void f1(Integer n) {
    log("f", n);
  }

  @DataProvider(parallel = true)
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
    log("f2", n);
  }
  
  private void log(String f, Integer n) {
    System.out.println("Thread:" + Thread.currentThread().getId() + " " + f + "(" + n + ")");
    try {
      Thread.sleep(Math.abs(new Random().nextLong() % 2000));
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void log(String f) {
    System.out.println("Thread:" + Thread.currentThread().getId() + " " + f + "()");
  }

}

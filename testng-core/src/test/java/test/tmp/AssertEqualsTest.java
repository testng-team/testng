package test.tmp;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

public class AssertEqualsTest {

  private void log(String s) {
    System.out.println("[" + Thread.currentThread().getId() + "] " + s);
  }

  @Test(threadPoolSize = 3, invocationCount = 6)
  public void f1() {
    log("start");
    try {
      int sleepTime = new Random().nextInt(500);
      Thread.sleep(sleepTime);
    }
    catch (Exception e) {
      log("  *** INTERRUPTED");
    }
    log("end");
  }

  @Test(threadPoolSize = 10, invocationCount = 10000)
  public void verifyMethodIsThreadSafe() {
//    foo();
  }

  @Test(dependsOnMethods = "verifyMethodIsThreadSafe")
  public void verify() {
    // make sure that nothing was broken
  }

  public static void main(String[] args) {
    Set set1 = new LinkedHashSet();
    Set set2 = new HashSet();

    set1.add(5);
    set2.add(5);

    set1.add(6);
    set2.add(6);

    set1.add(1);
    set2.add(1);

    set1.add(9);
    set2.add(9);

    System.out.println("set1 is:" + set1.toString());
    System.out.println("set2 is:" + set2.toString());

    System.out.println("is set1 equals set2 :" + set1.equals(set2));

    try {
      Assert.assertEquals(set1, set2, "set1 must equals with set2");
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
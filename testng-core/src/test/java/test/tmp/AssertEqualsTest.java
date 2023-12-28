package test.tmp;

import java.util.Random;
import org.testng.annotations.Test;

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
    } catch (Exception e) {
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
}

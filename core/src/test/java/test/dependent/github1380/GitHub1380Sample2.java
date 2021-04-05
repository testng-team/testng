package test.dependent.github1380;

import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;

public class GitHub1380Sample2 {

  @Test(dependsOnMethods = "testMethodB", priority = 3)
  public void testMethodA() {}

  @Test(priority = 2)
  public void testMethodB() throws InterruptedException {
    TimeUnit.SECONDS.sleep(5);
  }

  @Test(priority = 1)
  public void testMethodC() {}
}

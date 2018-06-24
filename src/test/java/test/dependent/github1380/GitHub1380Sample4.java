package test.dependent.github1380;

import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;

public class GitHub1380Sample4 {

  @Test(dependsOnMethods = "testMethodB", priority = 1)
  public void testMethodA() throws InterruptedException {
    TimeUnit.SECONDS.sleep(1);
  }

  @Test(priority = 2)
  public void testMethodB() {}

  @Test(priority = 3)
  public void testMethodC() {}
}

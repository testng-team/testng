package test.dependent.github1380;

import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;

public class GitHub1380Sample {

  @Test(priority = 1)
  public void testMethodA() {}

  @Test(dependsOnMethods = "testMethodA", priority = 2)
  public void testMethodB() throws InterruptedException {
    TimeUnit.SECONDS.sleep(1);
  }

  @Test(dependsOnMethods = "testMethodB", priority = 1)
  public void testMethodC() {}
}

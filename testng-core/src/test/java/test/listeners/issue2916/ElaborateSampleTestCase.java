package test.listeners.issue2916;

import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ElaborateSampleTestCase extends NormalSampleTestCase {

  @Test(priority = 2)
  public void failingTest() {
    Assert.fail();
  }

  @Test(dependsOnMethods = "failingTest")
  public void skippingTest() {}

  int counter = 1;

  @Test(successPercentage = 40, invocationCount = 4, priority = 3)
  public void flakyTest() {
    if (counter != 3) {
      Assert.fail();
    }
  }

  @Test(timeOut = 25, priority = 4)
  public void timingOutTest() throws InterruptedException {
    TimeUnit.MILLISECONDS.sleep(100);
    new Throwable().printStackTrace();
    if (counter != 3) {
      Assert.fail();
    }
  }
}

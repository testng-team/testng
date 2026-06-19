package test.listeners.issue2916;

import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;

public class ElaborateSampleTestCase extends NormalSampleTestCase {

  @Test(priority = 2)
  public void failingTest() {
    fail();
  }

  @Test(dependsOnMethods = "failingTest")
  public void skippingTest() {}

  int counter = 1;

  @Test(successPercentage = 40, invocationCount = 4, priority = 3)
  public void flakyTest() {
    if (counter != 3) {
      fail();
    }
  }

  @Test(timeOut = 25, priority = 4)
  public void timingOutTest() throws InterruptedException {
    TimeUnit.SECONDS.sleep(10);
    fail();
  }
}

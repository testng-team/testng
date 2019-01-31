package test.timeout.issue2009;

import org.testng.annotations.Test;

public class TimeOutWithParallelSample {
  @Test(timeOut = 1000)
  public void myTestMethod() throws InterruptedException {
    Thread.sleep(2000);
  }
}

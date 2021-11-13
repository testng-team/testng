package test.priority.issue2075;

import org.testng.annotations.Test;

public class InterruptTest {
  @Test(priority = 1)
  public void interruptsTheCurrentThread() {
    Thread.currentThread().interrupt();
  }

  @Test(priority = 2)
  public void shouldntGetInterruptedButDoes() throws InterruptedException {
    Thread.sleep(0); // this will throw an exception
  }
}

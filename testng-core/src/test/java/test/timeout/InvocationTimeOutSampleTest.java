package test.timeout;

import org.testng.annotations.Test;

public class InvocationTimeOutSampleTest {

  @Test(invocationCount = 5, invocationTimeOut = 2_000)
  public void shouldPass() {
    try {
      Thread.sleep(250);
    } catch (InterruptedException handled) {
      Thread.currentThread().interrupt();
    }
  }

  @Test(invocationCount = 5, invocationTimeOut = 1_000)
  public void shouldFail() {
    try {
      Thread.sleep(250);
    } catch (InterruptedException handled) {
      Thread.currentThread().interrupt();
    }
  }
}

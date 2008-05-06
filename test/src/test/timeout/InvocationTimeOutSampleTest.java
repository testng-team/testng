package test.timeout;

import org.testng.annotations.Test;

public class InvocationTimeOutSampleTest {

  @Test(invocationCount = 5, invocationTimeOut = 1300)
  public void shouldPass() {
    try {
      Thread.sleep(250);
    }
    catch (InterruptedException e) {
    }
  }

  @Test(invocationCount = 5, invocationTimeOut = 1000)
  public void shouldFail() {
    try {
      Thread.sleep(250);
    }
    catch (InterruptedException e) {
    }
  }
}

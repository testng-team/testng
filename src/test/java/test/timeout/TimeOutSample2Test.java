package test.timeout;

import org.testng.annotations.Test;

/**
 * Tests timeouts set from testng.xml
 * @author cbeust
 */
public class TimeOutSample2Test {
  @Test(timeOut = 1500)
  public void timeoutShouldFailByTimeOut() throws InterruptedException {
      Thread.sleep(10 * 1000);
  }

  public static void ppp(String s) {
    System.out.println("[TimeOutTest]@@@@@@@@@@@@@@@ " + s);
  }

}

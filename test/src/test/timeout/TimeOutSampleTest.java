package test.timeout;

import org.testng.annotations.*;

/**
 * This class tests timeouts
 * 
 * @author cbeust
 */
public class TimeOutSampleTest {
  
  @Test(timeOut = 5000 /* 5 seconds */)
  public void timeoutShouldPass() {
  }
  
  @Test(timeOut = 5 * 1000 /* 5 seconds */)
  public void timeoutShouldFailByException() {
    throw new RuntimeException("EXCEPTION SHOULD MAKE THIS METHOD FAIL");
  }
  
  @Test(timeOut = 1000 /* 1 second */)
  public void timeoutShouldFailByTimeOut() throws InterruptedException {
      Thread.sleep(10 * 1000 /* 10 seconds */);
  }
  
  public static void ppp(String s) {
    System.out.println("[TimeOutTest]@@@@@@@@@@@@@@@ " + s);
  }
}

package test.sample;



/**
 * This class tests timeouts
 *
 * @author cbeust
 */
public class TimeOutTest {
  /**
   * @testng.test timeOut="5000"
   */
  public void timeoutShouldPass() {
  }

  /**
   * @testng.test timeOut="5000"
   */
  public void timeoutShouldFail1() {
    throw new RuntimeException("EXCEPTION SHOULD MAKE THIS METHOD FAIL");
  }

  /**
   * @throws InterruptedException
   * @testng.test timeOut="1000"
   */
  public void timeoutShouldFail2() throws InterruptedException {
    Thread.sleep(5 * 1000);
    throw new RuntimeException("SHOULD HAVE BEEN INTERRUPTED BY TESTNG");
  }

  private static void ppp(String s) {
    System.out.println("[TimeOutTest] " + s);
  }
}

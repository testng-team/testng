package test.enabled;

/**
 * @testng.test timeOut="10000" enabled=false
 */
public class EnabledTest {

  /**
   * @testng.before-method
   */
  public void setup() throws InterruptedException {
    ppp("SETUP");
  }
  
  public void test1() {
    ppp("TEST1");
  }
  
  private void ppp(String s) {
    System.out.println("[EnabledTest] " + s);
  }
}

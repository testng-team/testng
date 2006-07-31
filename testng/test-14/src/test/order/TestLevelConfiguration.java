package test.order;


public class TestLevelConfiguration {
  /**
   * @testng.before-test
   */
  public void beforeTest() {
    System.out.println("TLC.beforeTest");
    throw new RuntimeException("should stop everythhing");
  }
  
  /**
   * @testng.after-test alwaysRun="true"
   */
  public void afterTest() {
    System.out.println("TLC.afterTest");
  }
}

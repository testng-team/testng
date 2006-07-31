package test.order;


public class TestLevelConfiguration {
  /**
   * @testng.configuration beforeTest="true"
   */
  public void beforeTest() {
    System.out.println("TLC.beforeTest");
    throw new RuntimeException("should stop everythhing");
  }
  
  /**
   * @testng.configuration afterTest="true" alwaysRun="true"
   */
  public void afterTest() {
    System.out.println("TLC.afterTest");
  }
}

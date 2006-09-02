package test.failedconfs;


public class Test2 {
  /**
   * @testng.test
   */
  public void testMethod1() {
    System.out.println("testMethod21");
    FailedBeforeTestMethodConfigurationBehaviorTest.s_noFailureClassMethods++;
  }
  
  /**
   * @testng.test
   */
  public void testMethod2() {
    System.out.println("testMethod22");
    FailedBeforeTestMethodConfigurationBehaviorTest.s_noFailureClassMethods++;
  }
}
package test.failedconfs;


public class Test1 {
  /**
   * @testng.before-method
   */
  public void failingBeforeTestMethod() {
    System.out.println("failingBeforeTestMethod");
    FailedBeforeTestMethodConfigurationBehaviorTest.s_failedBeforeTestMethodInvoked= true;
    throw new RuntimeException("expected exception thrown");
  }
  
  /**
   * @testng.test
   */
  public void testMethod1() {
    System.out.println("testMethod1");
    FailedBeforeTestMethodConfigurationBehaviorTest.s_failureClassMethods++;
  }
  
  /**
   * @testng.test
   */
  public void testMethod2() {
    System.out.println("testMethod2");
    FailedBeforeTestMethodConfigurationBehaviorTest.s_failureClassMethods++;
  }
}
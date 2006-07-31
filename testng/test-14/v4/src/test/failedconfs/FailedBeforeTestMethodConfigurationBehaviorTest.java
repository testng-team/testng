package test.failedconfs;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;

import testhelper.OutputDirectoryPatch;


public class FailedBeforeTestMethodConfigurationBehaviorTest {
  private static boolean s_failedBeforeTestMethodInvoked= false;
  private static int s_noFailureClassMethods;
  public static int s_failureClassMethods;
  
  /**
   * @testng.configuration beforeTestMethod = "true"
   */
  public void init() {
    s_failedBeforeTestMethodInvoked= false;
    s_noFailureClassMethods = 0;
    s_failureClassMethods = 0;
  }

  
  /**
   * @testng.test
   */
  public void beforeTestMethodFailureInTwoClasses() {
    TestNG testng = new TestNG();
    testng.setTestClasses(new Class[] { Test1.class, Test2.class });
    testng.setVerbose(0);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.run();
    
    Assert.assertTrue(s_failedBeforeTestMethodInvoked,
        "failing @BeforeTestMethod should have been invoked");
    Assert.assertEquals(s_noFailureClassMethods, 2, 
        "both methods in " + Test2.class.getName() + " should have been called");
    Assert.assertEquals(s_failureClassMethods, 0, 
        "no test method in " + Test1.class.getName() + " should have been called");
    
  }
  

  public static class Test1 {
    /**
     * @testng.configuration beforeTestMethod="true"
     */
    public void failingBeforeTestMethod() {
      s_failedBeforeTestMethodInvoked= true;
      throw new RuntimeException("expected exception thrown");
    }
    
    /**
     * @testng.test
     */
    public void testMethod1() {
      s_failureClassMethods++;
    }
    
    /**
     * @testng.test
     */
    public void testMethod2() {
      s_failureClassMethods++;
    }
  }
  
  public static class Test2 {
    /**
     * @testng.test
     */
    public void testMethod1() {
      s_noFailureClassMethods++;
    }
    
    /**
     * @testng.test
     */
    public void testMethod2() {
      s_noFailureClassMethods++;
    }
  }
}

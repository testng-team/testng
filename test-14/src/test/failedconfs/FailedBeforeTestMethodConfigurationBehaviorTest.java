package test.failedconfs;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;

import testhelper.OutputDirectoryPatch;


public class FailedBeforeTestMethodConfigurationBehaviorTest {
  static boolean s_failedBeforeTestMethodInvoked= false;
  static int s_noFailureClassMethods;
  public static int s_failureClassMethods;
  
  /**
   * @testng.before-method
   */
  public void init() {
    System.out.println("init");
    s_failedBeforeTestMethodInvoked= false;
    s_noFailureClassMethods = 0;
    s_failureClassMethods = 0;
  }

  
  /**
   * @testng.test enabled=false description="Test1 and Test2 are not triggered"
   */
  public void beforeTestMethodFailureInTwoClasses() {
    TestNG testng = new TestNG();
    testng.setSourcePath("./test-14/src;src");
    testng.setTarget("1.4");
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
}

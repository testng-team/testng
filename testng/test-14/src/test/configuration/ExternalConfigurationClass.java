package test.configuration;

import org.testng.Assert;


public class ExternalConfigurationClass {
  public static boolean s_afterMethod;
  public static boolean s_afterClass;
  public static boolean s_afterTest;
  
  /**
   * @testng.before-suite
   */
  public void beforeSuite() {
    MethodCallOrderTest.s_beforeSuite = true;
  }

  /**
   * @testng.before-method
   */
  public void beforeTest() {
    Assert.assertTrue(MethodCallOrderTest.s_beforeSuite);
    Assert.assertTrue(!MethodCallOrderTest.s_beforeTest);
    Assert.assertTrue(!MethodCallOrderTest.s_beforeClass);
    Assert.assertTrue(!MethodCallOrderTest.s_beforeMethod);
    
    MethodCallOrderTest.s_beforeTest = true;
  }
  
  /**
   * @testng.after-test
   */
  public void afterTest() {
    Assert.assertTrue(s_afterMethod, "afterTestMethod should have been run");
    Assert.assertTrue(s_afterClass, "afterTestClass should have been run");
    Assert.assertTrue(!s_afterTest, "afterTest should haven't been run");
    s_afterTest = true;
  }
  
  /**
   * @testng.after-suite
   */
  public void afterSuite() {
    Assert.assertTrue(s_afterMethod, "afterTestMethod should have been run");
    Assert.assertTrue(s_afterClass, "afterTestClass should have been run");
    Assert.assertTrue(s_afterTest, "afterTest should have been run");
    
    // clean up for the next run
    s_afterMethod = false;
    s_afterClass = false;
    s_afterTest = false;

  }
}

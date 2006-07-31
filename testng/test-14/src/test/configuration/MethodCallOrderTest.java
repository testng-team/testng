package test.configuration;

import org.testng.Assert;


public class MethodCallOrderTest {
  public static boolean s_beforeSuite = false;
  public static boolean s_beforeTest = false;
  public static boolean s_beforeClass = false;
  public static boolean s_beforeMethod = false;
  
  /**
   * @testng.before-suite
   */
  public void init() {
    ppp("@@@ INIT");
    // Seems to vary depending where I run it
//    assert ! s_beforeSuite : "BEFORESUITE SHOULD BE FALSE";
    s_beforeSuite = true;
    s_beforeTest = false;
    s_beforeClass = false;
    s_beforeMethod = false;
  }

  private void ppp(String string) {
    if (false) {
      System.out.println("[MethodCallOrderTest] " + string);
    }
  }

  /**
   * @testng.after-suite
   */
  public void cleanUp() {
    ppp("@@@ CLEAN UP");
    s_beforeSuite = false;
    s_beforeTest = false;
    s_beforeClass = false;
    s_beforeMethod = false;
  }

  /**
   * @testng.before-test
   */
  public void beforeTest() {
    Assert.assertTrue(s_beforeSuite);
    Assert.assertFalse(s_beforeTest);
    Assert.assertFalse(s_beforeClass);
    Assert.assertFalse(s_beforeMethod);
    
    s_beforeTest = true;
  }

  /**
   * @testng.before-class
   */
  public void beforeClass() {
    Assert.assertTrue(s_beforeSuite);
    Assert.assertTrue(s_beforeTest);
    Assert.assertTrue(!s_beforeClass);
    Assert.assertTrue(!s_beforeMethod);
    
    s_beforeClass = true;
  }
  
  /**
   * @testng.before-method
   */
  public void beforeMethod() {
    Assert.assertTrue(s_beforeSuite);
    Assert.assertTrue(s_beforeTest);
    Assert.assertTrue(s_beforeClass);
    Assert.assertTrue(!s_beforeMethod);
    s_beforeMethod = true;
  }
  
  /**
   * @testng.test
   */
  public void realTest() {
    Assert.assertTrue(s_beforeSuite);
    Assert.assertTrue(s_beforeTest);
    Assert.assertTrue(s_beforeClass);
    Assert.assertTrue(s_beforeMethod);
  }
  
  /**
   * @testng.after-method
   */
  public void afterMethod() {
    Assert.assertTrue(!ExternalConfigurationClass.s_afterMethod, "afterTestMethod shouldn't have been run");
    Assert.assertTrue(!ExternalConfigurationClass.s_afterClass, "afterTestClass shouldn't have been run");
    Assert.assertTrue(!ExternalConfigurationClass.s_afterTest, "afterTest should haven't been run");

    ExternalConfigurationClass.s_afterMethod = true;
  }
  
  /**
   * @testng.after-class
   */
  public void afterClass() {
    Assert.assertTrue(ExternalConfigurationClass.s_afterMethod, "afterTestMethod should have been run");
    Assert.assertTrue(!ExternalConfigurationClass.s_afterClass, "afterTestClass shouldn't have been run");
    Assert.assertTrue(!ExternalConfigurationClass.s_afterTest, "afterTest should haven't been run");
    ExternalConfigurationClass.s_afterClass = true;
  }
}

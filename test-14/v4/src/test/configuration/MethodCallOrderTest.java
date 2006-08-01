package test.configuration;

import org.testng.Assert;


public class MethodCallOrderTest {
  public static boolean s_beforeSuite;
  public static boolean s_beforeTest;
  public static boolean s_beforeClass;
  public static boolean s_beforeMethod;

  /**
   * @testng.configuration afterSuite="true"
   */
  public void cleanUp() {
    s_beforeSuite = false;
    s_beforeTest = false;
    s_beforeClass = false;
    s_beforeMethod = false;
  }

  /**
   * @testng.configuration beforeTestClass="true"
   */
  public void beforeClass() {
    Assert.assertTrue(s_beforeSuite);
    Assert.assertTrue(s_beforeTest);
    Assert.assertTrue(!s_beforeClass);
    Assert.assertTrue(!s_beforeMethod);
    
    s_beforeClass = true;
  }
  
  /**
   * @testng.configuration beforeTestMethod="true"
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
   * @testng.configuration afterTestMethod="true"
   */
  public void afterMethod() {
    Assert.assertTrue(!ExternalConfigurationClass.s_afterMethod, "afterTestMethod shouldn't have been run");
    Assert.assertTrue(!ExternalConfigurationClass.s_afterClass, "afterTestClass shouldn't have been run");
    Assert.assertTrue(!ExternalConfigurationClass.s_afterTest, "afterTest should haven't been run");

    ExternalConfigurationClass.s_afterMethod = true;
  }
  
  /**
   * @testng.configuration afterTestClass="true"
   */
  public void afterClass() {
    Assert.assertTrue(ExternalConfigurationClass.s_afterMethod, "afterTestMethod should have been run");
    Assert.assertTrue(!ExternalConfigurationClass.s_afterClass, "afterTestClass shouldn't have been run");
    Assert.assertTrue(!ExternalConfigurationClass.s_afterTest, "afterTest should haven't been run");
    ExternalConfigurationClass.s_afterClass = true;
  }
}

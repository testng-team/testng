package test.configuration;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class MethodCallOrderTest {
  public static boolean s_beforeSuite;
  public static boolean s_beforeTest;
  public static boolean s_beforeClass;
  public static boolean s_beforeMethod;

  @Configuration(beforeTestClass=true)
  public void beforeClass() {
    assertTrue(s_beforeSuite);
    assertTrue(s_beforeTest);
    assertFalse(s_beforeClass);
    assertFalse(s_beforeMethod);
    
    s_beforeClass = true;
  }
  
  @Configuration(afterSuite=true)
  public void cleanUp() {
    s_beforeSuite = false;
    s_beforeTest = false;
    s_beforeClass = false;
    s_beforeMethod = false;
  }

  
  @Configuration(beforeTestMethod=true)
  public void beforeMethod() {
    assertTrue(s_beforeSuite);
    assertTrue(s_beforeTest);
    assertTrue(s_beforeClass);
    assertFalse(s_beforeMethod);
    s_beforeMethod = true;
  }
  
  @Test
  public void realTest() {
    assertTrue(s_beforeSuite);
    assertTrue(s_beforeTest);
    assertTrue(s_beforeClass);
    assertTrue(s_beforeMethod);
  }
  
  @Configuration(afterTestMethod=true)
  public void afterMethod() {
    assertFalse(ExternalConfigurationClass.s_afterMethod, "afterTestMethod shouldn't have been run");
    assertFalse(ExternalConfigurationClass.s_afterClass, "afterTestClass shouldn't have been run");
    assertFalse(ExternalConfigurationClass.s_afterTest, "afterTest should haven't been run");

    ExternalConfigurationClass.s_afterMethod = true;
  }
  
  @Configuration(afterTestClass=true)
  public void afterClass() {
    assertTrue(ExternalConfigurationClass.s_afterMethod, "afterTestMethod should have been run");
    assertFalse(ExternalConfigurationClass.s_afterClass, "afterTestClass shouldn't have been run");
    assertFalse(ExternalConfigurationClass.s_afterTest, "afterTest should haven't been run");
    ExternalConfigurationClass.s_afterClass = true;
  }
}

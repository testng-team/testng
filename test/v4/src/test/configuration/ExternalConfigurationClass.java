package test.configuration;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Configuration;


public class ExternalConfigurationClass {
  public static boolean s_afterMethod;
  public static boolean s_afterClass;
  public static boolean s_afterTest;

  @Configuration(beforeSuite=true)
  public void beforeSuite() {
    MethodCallOrderTest.s_beforeSuite = true;
  }

  @Configuration(afterSuite=true)
  public void cleanUp() {
    s_afterMethod = false;
    s_afterClass = false;
    s_afterTest = false;
  }

  @Configuration(beforeTest=true)
  public void beforeTest() {
    assertTrue(MethodCallOrderTest.s_beforeSuite);
    assertFalse(MethodCallOrderTest.s_beforeTest);
    assertFalse(MethodCallOrderTest.s_beforeClass);
    assertFalse(MethodCallOrderTest.s_beforeMethod);
    
    MethodCallOrderTest.s_beforeTest = true;
  }
  
  @Configuration(afterTest=true)
  public void afterTest() {
    assertTrue(s_afterMethod, "afterTestMethod should have been run");
    assertTrue(s_afterClass, "afterTestClass should have been run");
    assertFalse(s_afterTest, "afterTest should haven't been run");
    s_afterTest = true;
  }
  
  @Configuration(afterSuite=true)
  public void afterSuite() {
    assertTrue(s_afterMethod, "afterTestMethod should have been run");
    assertTrue(s_afterClass, "afterTestClass should have been run");
    assertTrue(s_afterTest, "afterTest should have been run");
  }
}

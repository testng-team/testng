package test.configuration.sample;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

public class ExternalConfigurationClassSample {
  public static boolean s_afterMethod;
  public static boolean s_afterClass;
  public static boolean s_afterTest;

  @BeforeSuite
  public void beforeSuite() {
    MethodCallOrderTestSample.s_beforeSuite = true;
  }

  @AfterSuite
  public void cleanUp() {
    s_afterMethod = false;
    s_afterClass = false;
    s_afterTest = false;
  }

  @BeforeTest
  public void beforeTest() {
    assertTrue(MethodCallOrderTestSample.s_beforeSuite);
    assertFalse(MethodCallOrderTestSample.s_beforeTest);
    assertFalse(MethodCallOrderTestSample.s_beforeClass);
    assertFalse(MethodCallOrderTestSample.s_beforeMethod);

    MethodCallOrderTestSample.s_beforeTest = true;
  }

  @AfterTest
  public void afterTest() {
    assertTrue(s_afterMethod, "afterTestMethod should have been run");
    assertTrue(s_afterClass, "afterTestClass should have been run");
    assertFalse(s_afterTest, "afterTest should haven't been run");
    s_afterTest = true;
  }

  @AfterSuite
  public void afterSuite() {
    assertTrue(s_afterMethod, "afterTestMethod should have been run");
    assertTrue(s_afterClass, "afterTestClass should have been run");
    assertTrue(s_afterTest, "afterTest should have been run");
  }
}

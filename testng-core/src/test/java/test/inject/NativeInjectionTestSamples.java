package test.inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

class NativeInjectionTestSamples {

  public static class SimpleBase {
    @Test(groups = "test")
    public void testMethod() {}
  }

  public static class BadBeforeSuiteSample {
    @BeforeSuite(groups = "test")
    public void beforeSuite(int foo) {}
  }

  public static class BadBeforeTestSample {
    @BeforeTest(groups = "test")
    public void beforeTest(int foo) {}
  }

  public static class BadBeforeClassSample extends SimpleBase {
    @BeforeClass(groups = "test")
    public void beforeClass(int foo) {}
  }

  public static class BadBeforeMethodSample extends SimpleBase {
    @BeforeMethod(groups = "test")
    public void beforeMethod(int foo) {}
  }

  public static class BadAfterMethodSample extends SimpleBase {
    @AfterMethod(groups = "test")
    public void afterMethod(int foo) {}
  }

  public static class BadAfterClassSample extends SimpleBase {
    @AfterClass(groups = "test")
    public void afterClass(int foo) {}
  }

  public static class BadAfterTestSample extends SimpleBase {
    @AfterTest(groups = "test")
    public void afterTest(int foo) {}
  }

  public static class BadAfterSuiteSample extends SimpleBase {
    @AfterSuite(groups = "test")
    public void afterSuite(int foo) {}
  }

  public static class BadBeforeGroupsSample extends SimpleBase {
    @BeforeGroups(groups = "test")
    public void beforeGroups(int foo) {}
  }

  public static class BadAfterGroupsSample extends SimpleBase {
    @AfterGroups(groups = "test")
    public void afterGroups(int foo) {}
  }
}

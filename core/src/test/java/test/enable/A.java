package test.enable;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class A {

  public void testA() {}

  @Test
  public void testA2() {}

  @Test(enabled = true)
  public void testA3() {}

  @Test(enabled = false)
  public void disabledA() {}

  @BeforeSuite
  public void beforeSuiteA() {}

  @BeforeSuite(enabled = true)
  public void beforeSuiteA2() {}

  @BeforeSuite(enabled = false)
  public void disabledBeforeSuiteA() {}

  @BeforeSuite(alwaysRun = false)
  public void beforeSuiteNoRunA() {}

  @BeforeSuite(enabled = true, alwaysRun = false)
  public void beforeSuiteNoRunA2() {}

  @BeforeSuite(enabled = false, alwaysRun = false)
  public void disabledBeforeSuiteNoRunA() {}

  @BeforeSuite(alwaysRun = true)
  public void beforeSuiteRunA() {}

  @BeforeSuite(enabled = true, alwaysRun = true)
  public void beforeSuiteRunA2() {}

  @BeforeSuite(enabled = false, alwaysRun = true)
  public void disabledBeforeSuiteRunA() {}

  @AfterSuite
  public void afterSuiteA() {}

  @AfterSuite(enabled = true)
  public void afterSuiteA2() {}

  @AfterSuite(enabled = false)
  public void disabledAfterSuiteA() {}

  @AfterSuite(alwaysRun = false)
  public void afterSuiteNoRunA() {}

  @AfterSuite(enabled = true, alwaysRun = false)
  public void afterSuiteNoRunA2() {}

  @AfterSuite(enabled = false, alwaysRun = false)
  public void disabledAfterSuiteNoRunA() {}

  @AfterSuite(alwaysRun = true)
  public void afterSuiteRunA() {}

  @AfterSuite(enabled = true, alwaysRun = true)
  public void afterSuiteRunA2() {}

  @AfterSuite(enabled = false, alwaysRun = true)
  public void disabledAfterSuiteRunA() {}
}

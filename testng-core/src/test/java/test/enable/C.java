package test.enable;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

@Test
public class C {

  public void testC() {}

  @Test
  public void testC2() {}

  @Test()
  public void testC3() {}

  @Test(enabled = false)
  public void disabledC() {}

  @BeforeSuite
  public void beforeSuiteC() {}

  @BeforeSuite()
  public void beforeSuiteC2() {}

  @BeforeSuite(enabled = false)
  public void disabledBeforeSuiteC() {}

  @BeforeSuite()
  public void beforeSuiteNoRunC() {}

  @BeforeSuite()
  public void beforeSuiteNoRunC2() {}

  @BeforeSuite(enabled = false)
  public void disabledBeforeSuiteNoRunC() {}

  @BeforeSuite(alwaysRun = true)
  public void beforeSuiteRunC() {}

  @BeforeSuite(alwaysRun = true)
  public void beforeSuiteRunC2() {}

  @BeforeSuite(enabled = false, alwaysRun = true)
  public void disabledBeforeSuiteRunC() {}

  @AfterSuite
  public void afterSuiteC() {}

  @AfterSuite()
  public void afterSuiteC2() {}

  @AfterSuite(enabled = false)
  public void disabledAfterSuiteC() {}

  @AfterSuite()
  public void afterSuiteNoRunC() {}

  @AfterSuite()
  public void afterSuiteNoRunC2() {}

  @AfterSuite(enabled = false)
  public void disabledAfterSuiteNoRunC() {}

  @AfterSuite(alwaysRun = true)
  public void afterSuiteRunC() {}

  @AfterSuite(alwaysRun = true)
  public void afterSuiteRunC2() {}

  @AfterSuite(enabled = false, alwaysRun = true)
  public void disabledAfterSuiteRunC() {}
}

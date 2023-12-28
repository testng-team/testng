package test.enable;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

@Test(enabled = false)
public class B {

  public void testB() {}

  @Test
  public void testB2() {}

  @Test()
  public void testB3() {}

  @Test(enabled = false)
  public void disabledB() {}

  @BeforeSuite()
  public void disabledBeforeSuiteB() {}

  @BeforeSuite
  public void disabledBeforeSuiteB2() {}

  @BeforeSuite(enabled = false)
  public void disabledBeforeSuiteB3() {}

  @BeforeSuite()
  public void beforeSuiteNoRunB() {}

  @BeforeSuite()
  public void beforeSuiteNoRunB2() {}

  @BeforeSuite(enabled = false)
  public void disabledBeforeSuiteNoRunB() {}

  @BeforeSuite(alwaysRun = true)
  public void beforeSuiteRunB() {}

  @BeforeSuite(alwaysRun = true)
  public void beforeSuiteRunB2() {}

  @BeforeSuite(enabled = false, alwaysRun = true)
  public void disabledBeforeSuiteRunB() {}

  @AfterSuite
  public void afterSuiteB() {}

  @AfterSuite()
  public void afterSuiteB2() {}

  @AfterSuite(enabled = false)
  public void disabledAfterSuiteB() {}

  @AfterSuite()
  public void afterSuiteNoRunB() {}

  @AfterSuite()
  public void afterSuiteNoRunB2() {}

  @AfterSuite(enabled = false)
  public void disabledAfterSuiteNoRunB() {}

  @AfterSuite(alwaysRun = true)
  public void afterSuiteRunB() {}

  @AfterSuite(alwaysRun = true)
  public void afterSuiteRunB2() {}

  @AfterSuite(enabled = false, alwaysRun = true)
  public void disabledAfterSuiteRunB() {}
}

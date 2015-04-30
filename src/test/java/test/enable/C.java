package test.enable;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

@Test
public class C {

  public void testC() {}

  @Test
  public void testC2() {}

  @Test(enabled = true)
  public void testC3() {}

  @Test(enabled = false)
  public void disabledC() {}

  @BeforeSuite
  public void beforeSuiteC() {}

  @BeforeSuite(enabled = true)
  public void beforeSuiteC2() {}

  @BeforeSuite(enabled = false)
  public void disabledBeforeSuiteC() {}

  @BeforeSuite(alwaysRun = false)
  public void beforeSuiteNoRunC() {}

  @BeforeSuite(enabled = true, alwaysRun = false)
  public void beforeSuiteNoRunC2() {}

  @BeforeSuite(enabled = false, alwaysRun = false)
  public void disabledBeforeSuiteNoRunC() {}

  @BeforeSuite(alwaysRun = true)
  public void beforeSuiteRunC() {}

  @BeforeSuite(enabled = true, alwaysRun = true)
  public void beforeSuiteRunC2() {}

  @BeforeSuite(enabled = false, alwaysRun = true)
  public void disabledBeforeSuiteRunC() {}
}

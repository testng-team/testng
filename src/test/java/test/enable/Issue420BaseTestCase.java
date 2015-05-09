package test.enable;

import org.testng.annotations.BeforeSuite;

public abstract class Issue420BaseTestCase {

  @BeforeSuite(alwaysRun = true)
  public static void initEnvironment() {}
}

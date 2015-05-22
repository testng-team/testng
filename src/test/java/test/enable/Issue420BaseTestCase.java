package test.enable;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public abstract class Issue420BaseTestCase {

  @BeforeSuite(alwaysRun = true)
  public static void alwaysBeforeSuite() {}

  @BeforeSuite(alwaysRun = false)
  public static void beforeSuite() {}

  @AfterSuite(alwaysRun = false)
  public static void afterSuite() {}

  @AfterSuite(alwaysRun = true)
  public static void alwaysAfterSuite() {}
}

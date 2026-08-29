package test.configuration.issue2663;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/** Suite level configuration methods of one class, ordered by priority against each other. */
public class SuiteConfigSample {

  @BeforeSuite(priority = 2)
  public void alphaBeforeSuite() {}

  @BeforeSuite(priority = 1)
  public void bravoBeforeSuite() {}

  @AfterSuite(priority = 2)
  public void alphaAfterSuite() {}

  @AfterSuite(priority = 1)
  public void bravoAfterSuite() {}

  @Test
  public void test() {}
}

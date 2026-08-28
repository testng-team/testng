package test.configuration.issue2663;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/** Belongs to the second &lt;test&gt; of the two test suite, and carries the lowest priority. */
public class SecondTestSuiteConfigSample {

  @BeforeSuite(priority = 0)
  public void secondTestBeforeSuite() {}

  @Test
  public void secondTest() {}
}

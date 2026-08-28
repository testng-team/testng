package test.configuration.issue2663;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/** Belongs to the first &lt;test&gt; of the two test suite, and carries the highest priority. */
public class FirstTestSuiteConfigSample {

  @BeforeSuite(priority = 9)
  public void firstTestBeforeSuite() {}

  @Test
  public void firstTest() {}
}

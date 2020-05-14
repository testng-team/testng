package test;

import org.testng.annotations.Test;

/**
 * This class
 *
 * @author cbeust
 */
public class Misc extends BaseTest {

  @Test
  public void makeSureSetUpWithParameterWithNoParametersFails() {
    addClass("test.sample.SetUpWithParameterTest");
    setVerbose(0);
//    setParallel(XmlSuite.PARALLEL_METHODS);
    run();
    String[] passed = {
      };
      // @Configuration failures are not reported in the ITestListener
      String[] failed = {
      };
      String[] skipped = {
          "test",
      };
      verifyTests("Passed", passed, getPassedTests());
      verifyTests("Failed", failed, getFailedTests());
      verifyTests("Failed", skipped, getSkippedTests());
  }

}

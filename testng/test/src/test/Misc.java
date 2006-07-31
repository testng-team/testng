package test;

import org.testng.annotations.*;

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
    setParallel(false);
    run();
    String[] passed = {
      };
      String[] failed = {
          "setUp",
      };
      String[] skipped = {
          "test",
      };
      verifyTests("Passed", passed, getPassedTests());
      verifyTests("Failed", failed, getFailedTests());
      verifyTests("Failed", skipped, getSkippedTests());
  }

}

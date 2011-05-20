package test.timeout;

import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import test.BaseTest;


/**
 * This class
 *
 * @author cbeust
 */
public class TimeOutTest extends BaseTest {
  private Long m_id;

  public TimeOutTest() {
    m_id = Long.valueOf(System.currentTimeMillis());
  }

  @Test
  public void timeOutInParallel() {
    addClass("test.timeout.TimeOutSampleTest");
    setParallel(XmlSuite.PARALLEL_METHODS);
    run();
    String[] passed = {
        "timeoutShouldPass",
      };
      String[] failed = {
        "timeoutShouldFailByException", "timeoutShouldFailByTimeOut"
      };

//      dumpResults("Passed", getPassedTests());

      verifyTests("Passed", passed, getPassedTests());
      verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void timeOutInNonParallel() {
    addClass("test.timeout.TimeOutSampleTest");
    run();
    String[] passed = {
        "timeoutShouldPass",
      };
      String[] failed = {
          "timeoutShouldFailByException", "timeoutShouldFailByTimeOut"
      };
      verifyTests("Passed", passed, getPassedTests());
      verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void verifyInvocationTimeOut() {
    addClass("test.timeout.InvocationTimeOutSampleTest");
    run();
    String[] passed = {
        "shouldPass",
      };
      String[] failed = {
          "shouldFail"
      };
      verifyTests("Passed", passed, getPassedTests());
      verifyTests("Failed", failed, getFailedTests());
  }

  @Override
  public Long getId() {
    return m_id;
  }


}

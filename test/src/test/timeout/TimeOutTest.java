package test.timeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.BaseTest;


/**
 * This class
 * 
 * @author cbeust
 */
public class TimeOutTest extends BaseTest {
  private Long m_id;
  
  public TimeOutTest() {
    m_id = new Long(System.currentTimeMillis());     
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
  
  public Long getId() {
    return m_id;
  }
  
  
}

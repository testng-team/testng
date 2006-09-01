package test;

import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;


/**
 * This class
 * 
 * @author cbeust
 */
public class Test3 extends BaseTest {
  private Long m_id;
  
  public Test3() {
    m_id = new Long(System.currentTimeMillis());     
  }
  
  @Test
  public void timeOutInParallel() {
    addClass("test.sample.TimeOutTest");
    setParallel(XmlSuite.PARALLEL_METHODS);
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

  @Test(enabled=false)
  public void timeOutInNonParallel() {
    addClass("test.sample.TimeOutTest");
    setParallel(XmlSuite.PARALLEL_METHODS);
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

  @Override
  protected Long getId() {
    return m_id;
  }
  
  
}

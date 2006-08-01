package test;




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
  
   /**
    * @testng.test
    */  
  public void timeOut() {
    addClass("test.sample.TimeOutTest");
    setParallel(true);
    run();
    String[] passed = {
        "timeoutShouldPass", 
      };
      String[] failed = {
          "timeoutShouldFail1", "timeoutShouldFail2"
      };
      verifyTests("Passed", passed, getPassedTests());
      verifyTests("Failed", failed, getFailedTests());
  }

  protected Long getId() {
    return m_id;
  } 
}

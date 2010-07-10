package test.testng106;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;


/**
 * TESTNG-106: failing @BeforeSuite doesn't skip all tests 
 */
public class FailingSuiteFixture {
  static int s_invocations = 0;
  
  @BeforeSuite
  public void failingBeforeSuite() {
    double d = 1/0;
  }
  
  @AfterSuite(alwaysRun=true)
  public void afterSuite() {
    System.out.println("Invocations:" + s_invocations + " must be 0");
    Assert.assertEquals(s_invocations, 0, "@BeforeSuite has failed. All tests should be skipped.");
  }
}

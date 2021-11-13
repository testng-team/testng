package test_result;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class GitHub1197Sample {

  @Test
  public void failedTest() {
    Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
  }

  @Test(expectedExceptions = IllegalStateException.class, enabled = false)
  public void failedTest2() {
    Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
    throw new IllegalStateException();
  }

  @Test
  public void skippedTest() {
    Reporter.getCurrentTestResult().setStatus(ITestResult.SKIP);
  }

  @Test
  public void succeedTest() {
    Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
    Assert.fail();
  }

  @Test
  public void succeedTest2() {
    Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
    throw new IllegalStateException();
  }
}

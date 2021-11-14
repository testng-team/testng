package test.retryAnalyzer.github2669;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class RetryTestSample {
  public static int count = 0;

  @Parameters({"id", "name", "age"})
  @Test(retryAnalyzer = FailedRetry.class)
  public void create(String id, String name, String age, ITestContext context) {
    count++;
    Assert.fail();
  }
}

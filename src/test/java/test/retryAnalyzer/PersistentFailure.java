package test.retryAnalyzer;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PersistentFailure {
  @Test(retryAnalyzer = MyRetry.class)
  public void test() {
    Assert.fail();
  }
}

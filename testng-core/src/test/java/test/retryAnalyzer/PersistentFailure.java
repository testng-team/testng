package test.retryAnalyzer;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class PersistentFailure {
  @Test(retryAnalyzer = MyRetry.class)
  public void test() {
    fail();
  }
}

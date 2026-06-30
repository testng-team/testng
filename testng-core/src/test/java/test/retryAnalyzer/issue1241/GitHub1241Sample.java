package test.retryAnalyzer.issue1241;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class GitHub1241Sample {
  @Test(retryAnalyzer = MyRetry.class)
  public void test1() {}

  @Test(retryAnalyzer = MyRetry.class)
  public void test2() {
    fail();
  }
}

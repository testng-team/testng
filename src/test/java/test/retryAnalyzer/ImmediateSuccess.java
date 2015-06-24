package test.retryAnalyzer;

import org.testng.annotations.Test;

public class ImmediateSuccess {
  @Test(retryAnalyzer = MyRetry.class)
  public void test() {
  }
}

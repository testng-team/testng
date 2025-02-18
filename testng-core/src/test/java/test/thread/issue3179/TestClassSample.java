package test.thread.issue3179;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassSample {
  @DataProvider(parallel = true)
  public Object[] dp() {
    return new Object[] {1};
  }

  @Test(dataProvider = "dp")
  public void test1(int dp) {}

  @Test(dataProvider = "dp")
  public void test3(int dp) {}

  @Test
  public void test2() {}

  @Test
  public void test4() {}
}

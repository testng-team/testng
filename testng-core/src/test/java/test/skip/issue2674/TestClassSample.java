package test.skip.issue2674;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassSample {
  @Test
  void test1() {
    fail();
  }

  @Test(
      dataProvider = "items",
      dependsOnMethods = {"test1"})
  void test2(String model, int variant) {}

  @DataProvider(name = "items")
  Object[][] items() {
    return new Object[][] {{"iPhone", 13}, {"iPhone-Pro", 12}};
  }
}

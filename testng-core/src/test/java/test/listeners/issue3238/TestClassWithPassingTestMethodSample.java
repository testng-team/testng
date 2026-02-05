package test.listeners.issue3238;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassWithPassingTestMethodSample {
  @Test
  public void testMethod() {}

  @Test
  public void anotherTestMethod() {}

  @Test(dataProvider = "dp")
  public void dataDrivenTest(int ignored) {}

  @DataProvider(name = "dp", parallel = true)
  public Object[][] dp() {
    return new Object[][] {{1}, {2}, {3}};
  }
}

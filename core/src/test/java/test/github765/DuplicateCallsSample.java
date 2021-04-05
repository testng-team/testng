package test.github765;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DuplicateCallsSample extends TestTemplate<Integer> {

  private int i = 0;

  @Test(dataProvider = "testParameters")
  public void callExecuteTest(Integer testParameters) {
    Assert.assertTrue(testParameters > 0);
  }

  @DataProvider(name = "testParameters")
  public Object[][] getOnboardingTestParameters() {
    return new Object[][] {{4}};
  }
}

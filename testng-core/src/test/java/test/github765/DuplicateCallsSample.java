package test.github765;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DuplicateCallsSample extends TestTemplate<Integer> {

  @Test(dataProvider = "testParameters")
  public void callExecuteTest(Integer testParameters) {
    assertThat(testParameters).isPositive();
  }

  @DataProvider(name = "testParameters")
  public Object[][] getOnboardingTestParameters() {
    return new Object[][] {{4}};
  }
}

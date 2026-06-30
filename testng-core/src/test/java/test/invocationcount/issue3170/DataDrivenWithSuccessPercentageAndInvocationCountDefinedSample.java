package test.invocationcount.issue3170;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataDrivenWithSuccessPercentageAndInvocationCountDefinedSample {
  @Test(dataProvider = "test", invocationCount = 10, successPercentage = 90)
  public void sampleTestCase(String string) {
    assertThat(string).isEqualTo("1");
  }

  @DataProvider(name = "test")
  public Object[][] testProvider() {
    return new Object[][] {{"2"}};
  }
}

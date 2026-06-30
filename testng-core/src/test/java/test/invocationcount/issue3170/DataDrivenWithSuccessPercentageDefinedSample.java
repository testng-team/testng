package test.invocationcount.issue3170;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataDrivenWithSuccessPercentageDefinedSample {
  @Test(dataProvider = "test", successPercentage = 99)
  public void sampleTestCase(String string) {
    assertThat(string).isEqualTo("1");
  }

  @DataProvider(name = "test")
  public Object[][] testProvider() {
    return new Object[][] {{"1"}, {"2"}, {"3"}, {"4"}};
  }
}

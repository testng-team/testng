package test.retryAnalyzer;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Issue217TestClassSampleWithOneDataProvider {
  @Test(dataProvider = "dp")
  public void testMethod(int i) {
    assertThat(i).isPositive();
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    throw new RuntimeException("Simulating a failure");
  }
}

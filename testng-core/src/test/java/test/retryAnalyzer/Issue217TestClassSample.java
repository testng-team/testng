package test.retryAnalyzer;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Issue217TestClassSample {
  @Test
  public void a() {}

  @Test(dataProvider = "dp")
  public void testMethod(int i) {
    assertThat(i > 0).isTrue();
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    throw new RuntimeException("Simulating a failure");
  }
}

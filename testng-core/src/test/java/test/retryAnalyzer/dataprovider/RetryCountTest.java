package test.retryAnalyzer.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class RetryCountTest {

  @DataProvider
  public Object[][] provider() {
    return new Object[][] {{"a"}, {"b"}, {"c"}, {"d"}};
  }

  @Test(dataProvider = "provider", retryAnalyzer = DataProviderRetryAnalyzer.class)
  public void test1(String param) {
    assertThat(param).isEqualTo("c");
  }
}
